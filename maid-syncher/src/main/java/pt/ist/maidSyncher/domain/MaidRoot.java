/*******************************************************************************
 * Copyright (c) 2013 Instituto Superior Técnico - João Antunes
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Luis Silva - ACGHSync
 *     João Antunes - initial API and implementation
 ******************************************************************************/
package pt.ist.maidSyncher.domain;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.egit.github.core.client.GitHubClient;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.maidSyncher.api.activeCollab.ACContext;
import pt.ist.maidSyncher.domain.SyncEvent.SyncUniverse;
import pt.ist.maidSyncher.domain.SyncEvent.TypeOfChangeEvent;
import pt.ist.maidSyncher.domain.activeCollab.ACInstance;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.exceptions.SyncActionError;
import pt.ist.maidSyncher.domain.exceptions.SyncEventIllegalConflict;
import pt.ist.maidSyncher.domain.exceptions.SyncEventOriginObjectChanged;
import pt.ist.maidSyncher.domain.github.GHOrganization;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class MaidRoot extends MaidRoot_Base {

    private static Multimap<DSIObject, SyncEvent> changesBuzz = HashMultimap.create();

    private static GitHubClient gitHubClient;

    private static Properties configurationProperties;

    private static void initProperties() {
        configurationProperties = new Properties();
        InputStream configurationInputStream = MaidRoot.class.getResourceAsStream("/configuration.properties");
        if (configurationInputStream != null) {
            try {
                configurationProperties.load(configurationInputStream);
            } catch (IOException e) {
                throw new Error(e);
            }
        }
    }

    static {
        if (configurationProperties == null || configurationProperties.isEmpty()) {
            initProperties();
        }
        ACContext acContext = ACContext.getInstance();

        if (acContext.getServer() == null || StringUtils.isBlank(acContext.getServer())) {
            acContext.setServer(configurationProperties.getProperty("ac.server.host"));
            acContext.setToken(configurationProperties.getProperty("ac.server.token"));
        }
        if (getGitHubClient() == null) {
            //let's try to connect to the GH Account

            //let's try to authenticate and get the user and repository list
            setGitHubClient(new GitHubClient());

            String oauth2Token = configurationProperties.getProperty("github.oauth2.token");
            getGitHubClient().setOAuth2Token(oauth2Token);
        }
    }

    public static MaidRoot getInstance() {
        if (FenixFramework.getDomainRoot().getMaidRoot() == null) {
            initialize();
        }
        return FenixFramework.getDomainRoot().getMaidRoot();
    }

    @Atomic
    private static void initialize() {
        if (FenixFramework.getDomainRoot().getMaidRoot() == null) {
            FenixFramework.getDomainRoot().setMaidRoot(new MaidRoot(FenixFramework.getDomainRoot()));
        }
    }

    public MaidRoot(DomainRoot domainRoot) {
        super();
        domainRoot.setMaidRoot(this);
        checkIfIsSingleton();
        init();
    }

    public void init() {
        GHOrganization ghOrganization = getGhOrganization();
        if (ghOrganization == null)
            setGhOrganization(new GHOrganization());
        ACInstance acInstance = getAcInstance();
        if (acInstance == null)
            setAcInstance(new ACInstance(this));

    }

    private void checkIfIsSingleton() {
        if (FenixFramework.getDomainRoot().getMaidRoot() != null && FenixFramework.getDomainRoot().getMaidRoot() != this) {
            throw new Error("There can only be one! (instance of MyOrg [aka MaidRoot])");
        }
    }

    /**
     * Clears the {@link #changesBuzz}
     * 
     */
    public void resetSyncEvents() {
        this.changesBuzz = HashMultimap.create();

    }

    /**
     * Applies the changes in the changes buzz, by calling {@link SynchableObject#sync(SyncEvent)}
     * 
     * If any of those methods does not take care of one of the property descriptors, that event is logged
     * 
     * @throws SyncEventOriginObjectChanged if there was a change in one of the objects meanwhile
     */
    public void processChangesBuzz() throws SyncEventOriginObjectChanged {
        processChangesBuzz(false);
    }

    public void processChangesBuzz(boolean dryRun) throws SyncEventOriginObjectChanged {
        //ok, let's validate the current changes buzz before doing anything
        checkChangesBuzzApiObjectsAreEqual();
        List<SyncActionWrapper> syncActions = new ArrayList<>();
        for (DSIObject keyDSIObject : changesBuzz.keySet()) {
            for (SyncEvent syncEvent : changesBuzz.get(keyDSIObject)) {
                //let's now sync this
                SyncActionWrapper syncAction = syncEvent.getOriginObject().sync(syncEvent);
                if (syncAction != null) {
                    Collection<PropertyDescriptor> propertyDescriptorsTicked = syncAction.getPropertyDescriptorsTicked();
                    Set<PropertyDescriptor> changedPropertyDescriptors = syncEvent.getChangedPropertyDescriptors();
                    if (propertyDescriptorsTicked.containsAll(changedPropertyDescriptors) == false)
                        throw new Error("Not all of the changed fields were considering when processing "
                                + syncEvent.getApiObjectWrapper().getAPIObject().getClass().getSimpleName() + " " + syncEvent);
                    syncActions.add(syncAction);

                }
            }
        }

        //let's apply the changes
        for (SyncActionWrapper actionWrapper : syncActions) {
            actionWrapper.getOriginatingSyncEvent().getApiObjectWrapper().validateAPIObject();
            if (!dryRun)
                try {
                    actionWrapper.sync();
                } catch (IOException e) {
                    throw new SyncActionError("trying to sync. SyncEvent: " + actionWrapper.getOriginatingSyncEvent(), e);
                }
        }

    }

    private void checkChangesBuzzApiObjectsAreEqual() {
        //TODO issue #6
//        //ok, let's iterate through all of them
//        for (DSIObject keyDSIObject  : changesBuzz.keySet())
//        {
//            List<ACObject> acObjects = new ArrayList<>();
//            List<Object> ghObjects = new ArrayList<>();
//            for (SyncEvent syncEvent : changesBuzz.get(keyDSIObject))
//            {
//                //let's get the events different api objects
//                //and check afterwards if they are the same or not
//                syncEvent.getApiObjectWrapper().getAPIObject()
//            }
//        }

    }

    /**
     * Adds the given {@link SyncEvent} to the list of sync events.
     * 
     * <p>
     * It also looks out for possible errors, and throws a {@link SyncEventIllegalConflict} if they are found.
     * </p>
     * 
     * <p>
     * The behavior of the method, is as follows:
     * </p>
     * <p>
     * Checks for the given {@link SyncEvent#getDsiElement()} if there are other events, if there are the behaviour is as follows:
     * </p>
     * <ul>
     * <li>If two {@link TypeOfChangeEvent} events of the type {@link TypeOfChangeEvent#CREATE} are found, with different
     * {@link SyncUniverse}s, an error is thrown;</li>
     * <li>Two {@link TypeOfChangeEvent} of type {@link TypeOfChangeEvent#CREATE}, but with different {@link SyncUniverse}
     * targets, we simply log that, and remove both events (in the end, this means that both objects are already created on both
     * universes)</li>
     * <li>A {@link TypeOfChangeEvent#READ} is neutral (although several reads of the same target, could be squashed to just one)</li>
     * <li>If we get a {@link TypeOfChangeEvent#CREATE} event, and an {@link TypeOfChangeEvent#UPDATE} one, we are conservative
     * and throw an error (as they shouldn't occur for now, because we either create a new object on the other side, or we need to
     * update it) TODO on each case that occurs, see what really happen and change this behaviour</li>
     * <li>If we get an {@value TypeOfChangeEvent#UPDATE} longside with an {@link TypeOfChangeEvent#UPDATE} or
     * {@link TypeOfChangeEvent#DELETE}, we have a conflict solved by date, i.e. the one that wins is the one with the most recent
     * {@link SyncEvent#getDateOfChange()}</li>
     * 
     * 
     * 
     * 
     * 
     * @throws SyncEventIllegalConflict if there are sync errors, see the behavior above
     * @param syncEvent
     */
    protected void addSyncEvent(SyncEvent syncEvent) throws SyncEventIllegalConflict {
        DSIObject dsiElement = syncEvent.getDsiElement();
        boolean addEvent = true;
        List<SyncEvent> syncEventsToDelete = new ArrayList<>();
        if (dsiElement == null) {
            changesBuzz.put(null, syncEvent);
        } else if (changesBuzz.containsKey(dsiElement)) {
            scanExistingEvents: for (SyncEvent syncEventAlreadyPresent : changesBuzz.get(dsiElement)) {
                switch (syncEvent.getTypeOfChangeEvent()) {
                case CREATE:

                    /* CREATE */
                    switch (syncEventAlreadyPresent.getTypeOfChangeEvent()) {
                    case CREATE:
                        if (syncEvent.getTargetSyncUniverse().equals(syncEventAlreadyPresent.getTargetSyncUniverse())) {
                            throw new SyncEventIllegalConflict("Two sync events of the type Create for the DSIObject: "
                                    + dsiElement.getExternalId() + " with class: " + dsiElement.getClass().getName()
                                    + " were detected");
                        } else {
                            //let's remove the one that exists and do not add this one
                            addEvent = false;
                            syncEventsToDelete.add(syncEventAlreadyPresent);
                            break scanExistingEvents;
                        }
                    case READ:
                        break;
                    case UPDATE:
                        throwSyncUpdateAndCreateConflictException(dsiElement);
                    case DELETE:
                        //let's be conservative for now and throw an exception
                        //(this case might be 'legal' and lead to the whoever has the most
                        //recent date to prevail over the other, but let's see this case by case)
                        throw new SyncEventIllegalConflict("A sync event of Create and Delete over the same DSIObject: "
                                + dsiElement.getExternalId() + " class: " + dsiElement.getClass().getName() + " was detected");
                    }
                    break;
                /* end of CREATE */

                case READ:
                    //the READs are pretty much neutral
                    break;
                case UPDATE:
                    switch (syncEventAlreadyPresent.getTypeOfChangeEvent()) {
                    case CREATE:
                        throwSyncUpdateAndCreateConflictException(dsiElement);
                        break;
                    case READ:
                        break;
                    case UPDATE:
                    case DELETE:
                        //let's see the one that prevails, based on the date
                        addEvent = processUpdateAndDeleteOrUpdate(syncEvent, syncEventAlreadyPresent, syncEventsToDelete);
                        break scanExistingEvents;
                    }
                    break;
                case DELETE:
                    switch (syncEventAlreadyPresent.getTypeOfChangeEvent()) {
                    case CREATE:
                        //for now, let's throw an error, but this might be pheasible
                        throw new SyncEventIllegalConflict("a Delete with a Create was detected. dsiElement: "
                                + dsiElement.getExternalId() + " class: " + dsiElement.getClass().getSimpleName());
                    case READ:
                        break;
                    case UPDATE:
                    case DELETE:
                        //let's see the one that prevails, based on the date
                        addEvent = processUpdateAndDeleteOrUpdate(syncEvent, syncEventAlreadyPresent, syncEventsToDelete);
                        break scanExistingEvents;
                    }

                    break;
                }

            }

            //so, let's add if we have to, and delete the ones we should
            if (addEvent)
                changesBuzz.put(dsiElement, syncEvent);
            for (SyncEvent syncEventToDelete : syncEventsToDelete) {
                changesBuzz.remove(dsiElement, syncEventToDelete);
            }

        } else {
            changesBuzz.put(dsiElement, syncEvent);
        }
    }

    private boolean processUpdateAndDeleteOrUpdate(SyncEvent syncEvent, SyncEvent syncEventAlreadyPresent,
            List<SyncEvent> syncEventsToDelete) {
        if (syncEvent.getDateOfChange().compareTo(syncEventAlreadyPresent.getDateOfChange()) <= 0) {
            //the already present is more recent
            return false;
        } else {
            //the update, the new event, is more recent
            //let's delete this already present one
            syncEventsToDelete.add(syncEventAlreadyPresent);
            return true;
        }
    }

    private void throwSyncUpdateAndCreateConflictException(DSIObject dsiElement) throws SyncEventIllegalConflict {
        throw new SyncEventIllegalConflict("A sync event of the type Create and Update for the DSIObject: "
                + dsiElement.getExternalId() + " class: " + dsiElement.getClass().getName() + " were detected");

    }

    public static Multimap<DSIObject, SyncEvent> getChangesBuzz() {
        return changesBuzz;
    }

    public static GitHubClient getGitHubClient() {
        return gitHubClient;
    }

//    public void fullySync(User repository) {
//        checkNotNull(repository, "repository must not be null");
//        checkArgument(repository.getType().equals(User.TYPE_ORG), "You must provide a repository");
//
//    }

    public static void setGitHubClient(GitHubClient gitHubClient) {
        MaidRoot.gitHubClient = gitHubClient;
    }

//TODO make a getter for the ChangesBuzz

}
