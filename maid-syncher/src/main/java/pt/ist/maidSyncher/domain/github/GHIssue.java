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
package pt.ist.maidSyncher.domain.github;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jvstm.cps.ConsistencyPredicate;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.api.activeCollab.ACMilestone;
import pt.ist.maidSyncher.api.activeCollab.ACSubTask;
import pt.ist.maidSyncher.api.activeCollab.ACTask;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SyncEvent;
import pt.ist.maidSyncher.domain.SynchableObject;
import pt.ist.maidSyncher.domain.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.activeCollab.ACTaskCategory;
import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;
import pt.ist.maidSyncher.domain.dsi.DSIIssue;
import pt.ist.maidSyncher.domain.dsi.DSIMilestone;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.dsi.DSIProject;
import pt.ist.maidSyncher.domain.dsi.DSIRepository;
import pt.ist.maidSyncher.domain.dsi.DSISubTask;
import pt.ist.maidSyncher.domain.exceptions.SyncActionError;
import pt.ist.maidSyncher.domain.exceptions.SyncEventIncogruenceBetweenOriginAndDestination;
import pt.ist.maidSyncher.domain.sync.EmptySyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;

public class GHIssue extends GHIssue_Base {
    private static final Logger LOGGER = LoggerFactory.getLogger(GHIssue.class);

    public final static String STATE_CLOSED = "closed";
    public final static String STATE_OPEN = "open";

    //The PropertyDescriptor s of this issue
    protected final static String DSC_STATE = "state";
    protected final static String DSC_LABELS = "labels";
    protected final static String DSC_NUMBER = "number";
    protected final static String DSC_BODY = "body";
    protected final static String DSC_TITLE = "title";
    protected final static String DSC_UPDATED_AT = "updatedAt";
    protected final static String DSC_HTML_URL = "htmlUrl";

    public GHIssue() {
        super();
        MaidRoot.getInstance().addGhIssues(this);
    }

    static GHIssue process(Issue issue) {
        return process(issue, false);
    }

    static GHIssue process(Issue issue, boolean skipSync) {
        checkNotNull(issue);
        MaidRoot maidRoot = MaidRoot.getInstance();
        return (GHIssue) findOrCreateAndProccess(issue, GHIssue.class, maidRoot.getGhIssues(), skipSync);
    }

    @Service
    public static GHIssue process(Issue issue, Repository repository) {
        return process(issue, repository, false);

    }

    @Service
    public static GHIssue process(Issue issue, Repository repository, boolean skipSync) {
        checkNotNull(repository);
        //let's first take care of the issue, and then assign it the repository
        GHIssue ghIssue = process(issue, skipSync);
        GHRepository ghRepository = GHRepository.process(repository, skipSync);

        ghIssue.setRepository(ghRepository);

        return ghIssue;

    }

    @Override
    public Collection<PropertyDescriptor> copyPropertiesFrom(Object orig) throws IllegalAccessException,
    InvocationTargetException, NoSuchMethodException {
        checkNotNull(orig);
        checkArgument(orig instanceof Issue, "provided object must be an instance of " + Issue.class.getName());

        Set<PropertyDescriptor> changedPropertyDescriptors = new HashSet<>(super.copyPropertiesFrom(orig));

        Issue issue = (Issue) orig;
        //now let's take care of the relations with the other objects
        //like Milestone and Label
        Milestone milestone = issue.getMilestone();
        if (milestone != null) {
            GHMilestone ghMilestone = GHMilestone.process(milestone);
            if (!ObjectUtils.equals(getMilestone(), ghMilestone))
                changedPropertyDescriptors.add(getPropertyDescriptorAndCheckItExists(issue, "milestone"));
            setMilestone(ghMilestone);
        }

        Set<GHLabel> ghOldLabels = new HashSet<GHLabel>(getLabels());
        Set<GHLabel> newGHLabels = new HashSet<GHLabel>();
        for (Label label : issue.getLabels()) {
            GHLabel ghLabel = GHLabel.process(label);
            newGHLabels.add(ghLabel);
        }
        if (!ObjectUtils.equals(ghOldLabels, newGHLabels))
            changedPropertyDescriptors.add(getPropertyDescriptorAndCheckItExists(issue, "labels"));
        for (GHLabel ghLabel : getLabels()) {
            removeLabels(ghLabel);
        }
        for (GHLabel ghLabel : newGHLabels) {
            addLabels(ghLabel);
        }

        User assignee = issue.getAssignee();
        GHUser ghNewAssignee = null;
        if (assignee != null)
            ghNewAssignee = GHUser.process(assignee);
        GHUser ghOldAssignee = getAssignee();
        if (!ObjectUtils.equals(ghNewAssignee, ghOldAssignee))
            changedPropertyDescriptors.add(getPropertyDescriptorAndCheckItExists(issue, "assignee"));
        setAssignee(ghNewAssignee);
        return changedPropertyDescriptors;
    }

    @Override
    public LocalTime getUpdatedAtDate() {
        return getUpdatedAt() == null ? getCreatedAt() : getUpdatedAt();
    }

    @ConsistencyPredicate
    private boolean checkDSIObjectMultiplicity() {
        //we cannot be both an issue and a subtask
        if (hasDsiObjectIssue() && hasDsiObjectSubTask())
            return false;
        return true;
    }

    @Override
    protected DSIObject getDSIObject() {
        if (hasDsiObjectIssue())
            return getDsiObjectIssue();
        else
            return getDsiObjectSubTask();
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        //let's either make a DSISubTask or a DSIIssue
        //depending if we have a @subtask on the description or not
        DSIObject dsiObjectToReturn = getDSIObject();
        if (dsiObjectToReturn == null) {
            //we cannot create ACSubTasks through GH without using a comment on an
            //already existing issue, so, we assume it's a Issue
            dsiObjectToReturn = new DSIIssue();
        }
        return dsiObjectToReturn;
    }

    @Override
    public SyncActionWrapper sync(SyncEvent syncEvent) {
        SyncActionWrapper syncActionWrapperToReturn = null;
        if (syncEvent.getTypeOfChangeEvent().equals(SyncEvent.TypeOfChangeEvent.CREATE)) {
            //then we should need to create a GHIssue, let's just make sure that's correct
            if (syncEvent.getTargetSyncUniverse().equals(SyncEvent.SyncUniverse.GITHUB))
                throw new SyncEventIncogruenceBetweenOriginAndDestination("For syncEvent: " + syncEvent.toString());

            //if we are creating an artefact on AC side, it mustn't be a SubTask, it's a Task
            ACTask newAcTask = new ACTask();
            syncActionWrapperToReturn = syncCreateEvent(newAcTask, syncEvent);

        } else if (syncEvent.getTypeOfChangeEvent().equals(SyncEvent.TypeOfChangeEvent.UPDATE)) {
            //let's retrieve the existing ACTask or ACSubTask and use it to prefill the api object
            DSIObject dsiIssueOrSubTask = getDSIObject();
            if (dsiIssueOrSubTask instanceof DSIIssue) {
                DSIIssue dsiIssue = (DSIIssue) dsiIssueOrSubTask;
                pt.ist.maidSyncher.domain.activeCollab.ACTask acDomainTask = dsiIssue.getAcTask();
                ACTask newAcTask = new ACTask();
                try {
                    acDomainTask.copyPropertiesTo(newAcTask);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | TaskNotVisibleException e) {
                    throw new SyncActionError("Error trying to prefill the properties from the ACTask to the bean.", syncEvent, e);
                }

                syncActionWrapperToReturn = syncUpdateTaskEvent(newAcTask, dsiIssue, syncEvent);

            } else if (dsiIssueOrSubTask instanceof DSISubTask) {
                DSISubTask dsiSubTask = (DSISubTask) dsiIssueOrSubTask;
                pt.ist.maidSyncher.domain.activeCollab.ACSubTask acDomainSubTask = dsiSubTask.getAcSubTask();
                ACSubTask newAcSubTask = new ACSubTask();
                try {
                    acDomainSubTask.copyPropertiesTo(newAcSubTask);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | TaskNotVisibleException e) {
                    throw new SyncActionError("Error trying to prefill the properties from the ACSubTask to the bean.",
                            syncEvent, e);
                }

                syncActionWrapperToReturn = syncUpdateSubTaskEvent(newAcSubTask, dsiSubTask, syncEvent);

            }
        }

        else {
            LOGGER.warn("Read and Delete events not supported yet. " + syncEvent);
            syncActionWrapperToReturn = new EmptySyncActionWrapper(syncEvent);
        }

        return syncActionWrapperToReturn;
    }

    private SyncActionWrapper syncUpdateSubTaskEvent(ACSubTask newAcSubTask, DSISubTask dsiSubTask, SyncEvent syncEvent) {
        final Set<PropertyDescriptor> tickedDescriptors = new HashSet<>();
        for (PropertyDescriptor changedDescriptor : syncEvent.getChangedPropertyDescriptors()) {
            tickedDescriptors.add(changedDescriptor);
            switch (changedDescriptor.getName()) {
            case DSC_ID:
            case DSC_URL:
            case DSC_HTML_URL:
            case DSC_CREATED_AT:
            case DSC_UPDATED_AT:
            case DSC_NUMBER:
                break; //the ones above, there'se no sense in changing anything

            case DSC_LABELS:
            case DSC_STATE:
            case DSC_BODY:
            case DSC_TITLE:
                break;
            default:
                tickedDescriptors.remove(changedDescriptor); //if we did not fall on any of the above
                //cases, let's remove it from the ticked descriptors

            }
        }

        return null;
    }

    private SyncActionWrapper syncUpdateTaskEvent(ACTask newAcTask, DSIIssue dsiIssue, SyncEvent syncEvent) {
        // TODO Auto-generated method stub
        return null;
    }

    private SyncActionWrapper syncCreateEvent(final ACTask newAcTask, final SyncEvent syncEvent) {
        final Set<PropertyDescriptor> tickedDescriptors = new HashSet<>();
        for (PropertyDescriptor changedDescriptor : syncEvent.getChangedPropertyDescriptors()) {
            tickedDescriptors.add(changedDescriptor);
            switch (changedDescriptor.getName()) {
            case DSC_ID:
            case DSC_URL:
            case DSC_HTML_URL:
            case DSC_CREATED_AT:
            case DSC_UPDATED_AT:
            case DSC_NUMBER:

            case DSC_LABELS:
            case DSC_STATE:
            case DSC_BODY:
            case DSC_TITLE:
                break;
            default:
                tickedDescriptors.remove(changedDescriptor); //if we did not fall on any of the above
                //cases, let's remove it from the ticked descriptors

            }
        }

        SyncActionWrapper toReturnActionWrapper = new SyncActionWrapper() {

            @Override
            public Collection<SynchableObject> sync() throws IOException {


                //let's fill appropriately the newTask
                newAcTask.setName(getTitle());
                newAcTask.setBody(getBodyHtml());
                newAcTask.setVisibility(true);

                if (getState().equals(STATE_CLOSED)) {
                    newAcTask.setComplete(true);
                } else if (getState().equals(STATE_OPEN)) {
                    newAcTask.setComplete(false);
                }

                DSIRepository dsiRepository = (DSIRepository) getRepository().getDSIObject(); //depended on
                //we will also need the project on the AC side
                DSIIssue dsiIssue = (DSIIssue) getDSIObject(); /*seen that we are creating based on something
                                                               from GH, we are creating an issue, thus the cast */
                //let's find out which project we should be creating the task on
                //in this version, let's use the default one always.
                //TODO use the labels to create the task in various projects GHIssue #10

                final ACProject acProject = dsiRepository.getDefaultProject();
                //now, the harder stuff, Milestones, and Category [aka repository]

                //let's see if the Milestone exists
                if (getMilestone() != null) {

                    DSIMilestone dsiMilestone = (DSIMilestone) getMilestone().getDSIObject(); //depended on
                    if (dsiMilestone == null || dsiMilestone.getAcMilestone() == null) {
                        //we should create it
                        ACMilestone acMilestoneToCreate = getMilestone().getACCorrespondingPreliminarObject();
                        pt.ist.maidSyncher.domain.activeCollab.ACMilestone newMilestone =
                                pt.ist.maidSyncher.domain.activeCollab.ACMilestone.process(
                                        ACMilestone.create(acMilestoneToCreate), true);
                        if (dsiMilestone == null) {
                            dsiMilestone = (DSIMilestone) getMilestone().findOrCreateDSIObject();
                        }
                        dsiMilestone.setAcMilestone(newMilestone);

                    } else {
                        pt.ist.maidSyncher.domain.activeCollab.ACMilestone acMilestone = dsiMilestone.getAcMilestone();
                        if (ObjectUtils.equals(acMilestone.getProject(), acProject) == false) {
                            //then this milestone should be moved,or copied to the new project
                            //let's see which one we should do
                            boolean hasOtherTasks = acMilestone.getTasks().isEmpty() == false;

                            if (hasOtherTasks) {
                                //let's copy it
                                pt.ist.maidSyncher.domain.activeCollab.ACMilestone newMilestone =
                                        pt.ist.maidSyncher.domain.activeCollab.ACMilestone.process(ACMilestone.copyTo(
                                                acMilestone.getId(), acMilestone.getProject().getId(), acProject.getId()), true);
                                dsiMilestone.setAcMilestone(newMilestone);
                            } else {
                                //let's move it
                                ACMilestone.moveTo(acMilestone.getId(), acMilestone.getProject().getId(), acProject.getId());
                                //let's make the move internally without fuss
                                acMilestone.setProject(acProject);
                            }

                        }
                    }
                    newAcTask.setMilestoneId((int) dsiMilestone.getAcMilestone().getId());
                }


                ACTaskCategory acTaskCategory = dsiRepository.getACTaskCategoryFor(acProject);


                newAcTask.setCategoryId((int) acTaskCategory.getId());


                ACTask newlyCreatedTask = ACTask.createTask(newAcTask, acProject.getId());
                pt.ist.maidSyncher.domain.activeCollab.ACTask newlyCreatedDomainAcTask =
                        pt.ist.maidSyncher.domain.activeCollab.ACTask.process(newlyCreatedTask, acProject.getId(), true);

                dsiIssue.setAcTask(newlyCreatedDomainAcTask);
                return Collections.singleton((SynchableObject) newlyCreatedDomainAcTask);

            }

            @Override
            public Collection<DSIObject> getSyncDependedDSIObjects() {
                Set<DSIObject> dsiObjectsDependedOn = new HashSet<>();
//                dsiObjectsDependedOn.add(getMilestone().getDSIObject());
                dsiObjectsDependedOn.add(getRepository().getDSIObject());
                dsiObjectsDependedOn.add(null); //null - means all of the GHLabel's should be synched
                return dsiObjectsDependedOn;
            }

            @Override
            public Collection<PropertyDescriptor> getPropertyDescriptorsTicked() {
                return tickedDescriptors;
            }

            @Override
            public SyncEvent getOriginatingSyncEvent() {
                return syncEvent;
            }

            @Override
            public Set<Class> getSyncDependedTypesOfDSIObjects() {
                Set<Class> dependedOnDSIClasses = new HashSet<>();
                dependedOnDSIClasses.add(DSIMilestone.class);
                dependedOnDSIClasses.add(DSIProject.class);
                dependedOnDSIClasses.add(DSIRepository.class);
                return dependedOnDSIClasses;
            }
        };

        return toReturnActionWrapper;
    }

}
