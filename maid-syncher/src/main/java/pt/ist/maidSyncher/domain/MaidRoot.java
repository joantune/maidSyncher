package pt.ist.maidSyncher.domain;

import java.util.ArrayList;
import java.util.List;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.maidSyncher.domain.SyncEvent.SyncUniverse;
import pt.ist.maidSyncher.domain.SyncEvent.TypeOfChangeEvent;
import pt.ist.maidSyncher.domain.activeCollab.ACInstance;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.exceptions.SyncEventIllegalConflict;
import pt.ist.maidSyncher.domain.github.GHOrganization;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class MaidRoot extends MaidRoot_Base {

    private static Multimap<DSIObject, SyncEvent> changesBuzz = HashMultimap.create();

    public static MaidRoot getInstance() {
        return FenixFramework.getRoot();
    }

    public MaidRoot() {
        super();
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
        if (FenixFramework.getRoot() != null && FenixFramework.getRoot() != this) {
            throw new Error("There can only be one! (instance of MyOrg)");
        }
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
        if (changesBuzz.containsKey(dsiElement)) {
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

//    public void fullySync(User repository) {
//        checkNotNull(repository, "repository must not be null");
//        checkArgument(repository.getType().equals(User.TYPE_ORG), "You must provide a repository");
//
//    }

//TODO make a getter for the ChangesBuzz

}
