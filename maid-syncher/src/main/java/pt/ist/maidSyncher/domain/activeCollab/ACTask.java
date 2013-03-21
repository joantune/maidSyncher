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
package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MilestoneService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.api.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SyncEvent;
import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;
import pt.ist.maidSyncher.domain.dsi.DSIIssue;
import pt.ist.maidSyncher.domain.dsi.DSIMilestone;
import pt.ist.maidSyncher.domain.dsi.DSIObject;
import pt.ist.maidSyncher.domain.dsi.DSIProject;
import pt.ist.maidSyncher.domain.dsi.DSIRepository;
import pt.ist.maidSyncher.domain.exceptions.SyncActionError;
import pt.ist.maidSyncher.domain.exceptions.SyncEventIncogruenceBetweenOriginAndDestination;
import pt.ist.maidSyncher.domain.github.GHIssue;
import pt.ist.maidSyncher.domain.github.GHLabel;
import pt.ist.maidSyncher.domain.github.GHMilestone;
import pt.ist.maidSyncher.domain.github.GHRepository;
import pt.ist.maidSyncher.domain.github.GHUser;
import pt.ist.maidSyncher.domain.sync.EmptySyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.utils.MiscUtils;

public class ACTask extends ACTask_Base {

    public ACTask() {
        super();
    }

    private Collection<PropertyDescriptor> processMainAssignee(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) {

        ACUser acUser = ACUser.findById(acTask.getAssigneeId());
        ACUser oldMainAssignee = getMainAssignee();
        setMainAssignee(acUser);
        return !ObjectUtils.equals(acUser, oldMainAssignee) ? Collections.singleton(getPropertyDescriptorAndCheckItExists(acTask,
                "assigneeId")) : Collections.EMPTY_SET;
    }

    private Collection<PropertyDescriptor> processOtherAssignees(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) {

        boolean somethingChanged = false;
        Set<ACUser> newOtherAssigneesSet = new HashSet<ACUser>();

        Set<Long> otherAssigneesId = acTask.getOtherAssigneesId();

        for (Long userId : otherAssigneesId) {
            ACUser otherAssignee = ACUser.findById(userId);
            newOtherAssigneesSet.add(otherAssignee);
        }

        //retrieve the old
        HashSet<ACUser> oldSet = new HashSet<ACUser>(getOtherAssigneesSet());

        //now, let's compare
        if (!ObjectUtils.equals(newOtherAssigneesSet, oldSet)) {
            somethingChanged = true;
        }

        //now, let's substitute
        for (ACUser user : getOtherAssignees()) {
            removeOtherAssignees(user);
        }

        for (ACUser user : newOtherAssigneesSet)
            addOtherAssignees(user);
        return somethingChanged ? Collections.singleton(getPropertyDescriptorAndCheckItExists(acTask, "otherAssigneesId")) : Collections.EMPTY_SET;
    }

    private Collection<PropertyDescriptor> processMilestone(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) {
        ACMilestone newMilestone = ACMilestone.findById(acTask.getMilestoneId());
        ACMilestone oldMilestone = getMilestone();
        setMilestone(newMilestone);
        return !ObjectUtils.equals(oldMilestone, newMilestone) ? Collections.singleton(getPropertyDescriptorAndCheckItExists(
                acTask, "milestoneId")) : Collections.EMPTY_SET;

    }

    @Override
    public Collection<PropertyDescriptor> copyPropertiesFrom(Object orig) throws IllegalAccessException,
    InvocationTargetException,
    NoSuchMethodException {
        HashSet<PropertyDescriptor> changedDescriptors = new HashSet<>(super.copyPropertiesFrom(orig));

        pt.ist.maidSyncher.api.activeCollab.ACTask acTask = (pt.ist.maidSyncher.api.activeCollab.ACTask) orig;
        //now let's take care of the milestone, main assignee and other assignees

        changedDescriptors.addAll(processMainAssignee(acTask));

        changedDescriptors.addAll(processOtherAssignees(acTask));

        changedDescriptors.addAll(processMilestone(acTask));

        changedDescriptors.addAll(processCategory(acTask));

        changedDescriptors.addAll(processLabel(acTask));


        return changedDescriptors;

    }

    public static ACTask findById(long id) {
        return (ACTask) MiscUtils.findACObjectsById(id, ACTask.class);
    }

    private Collection<PropertyDescriptor> processLabel(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) {
        ACTaskLabel newTaskLabel = ACTaskLabel.findById(acTask.getLabelId());
        ACTaskLabel oldTaskLabel = getLabel();
        setLabel(newTaskLabel);
        return !ObjectUtils.equals(newTaskLabel, oldTaskLabel) ? Collections.singleton(getPropertyDescriptorAndCheckItExists(
                acTask, "labelId")) : Collections.EMPTY_SET;

    }

    private Collection<PropertyDescriptor> processCategory(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) {
        ACTaskCategory newTaskCategory = ACTaskCategory.findById(acTask.getCategoryId());
        ACTaskCategory oldTaskCategory = getTaskCategory();
        setTaskCategory(newTaskCategory);
        return !ObjectUtils.equals(newTaskCategory, oldTaskCategory) ? Collections
                .singleton(getPropertyDescriptorAndCheckItExists(acTask, "categoryId")) : Collections.EMPTY_SET;
    }

    @Service
    static ACTask process(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) throws TaskNotVisibleException {
        return process(acTask, false);
    }

    @Service
    static ACTask process(pt.ist.maidSyncher.api.activeCollab.ACTask acTask, boolean skipSync) throws TaskNotVisibleException {
        checkNotNull(acTask);
        //let's check on the visibility
        if (acTask.getVisibility() == false)
            throw new TaskNotVisibleException();
        return (ACTask) findOrCreateAndProccess(acTask, ACTask.class, MaidRoot.getInstance().getAcObjects(), skipSync);
    }

    @Service
    public static ACTask process(pt.ist.maidSyncher.api.activeCollab.ACTask task, ACProject project) {
        checkNotNull(project);

        pt.ist.maidSyncher.domain.activeCollab.ACProject acDomainProject =
                pt.ist.maidSyncher.domain.activeCollab.ACProject.process(project);

        ACTask acDomainTask;
        try {
            acDomainTask = process(task);
        } catch (TaskNotVisibleException e) {
            return null;
        }
        acDomainTask.setProject(acDomainProject);

        return acDomainTask;
    }

    @Service
    public static ACTask process(pt.ist.maidSyncher.api.activeCollab.ACTask task, long projectId, boolean skipSync) {
        checkNotNull(task);

        pt.ist.maidSyncher.domain.activeCollab.ACProject acDomainProject =
                pt.ist.maidSyncher.domain.activeCollab.ACProject.findById(projectId);

        ACTask acDomainTask;
        try {
            acDomainTask = process(task, skipSync);
        } catch (TaskNotVisibleException e) {
            return null;
        }
        acDomainTask.setProject(acDomainProject);

        return acDomainTask;
    }

    public Set<ACUser> getAssignees() {
        HashSet<ACUser> toReturn = new HashSet<ACUser>();
        toReturn.addAll(getOtherAssignees());
        toReturn.add(getMainAssignee());

        return toReturn;

    }

    @Override
    protected DSIObject getDSIObject() {
        return getDsiObjectIssue();
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        DSIObject dsiObject = getDSIObject();
        if (dsiObject == null) {
            dsiObject = new DSIIssue();
            setDsiObjectIssue((DSIIssue) dsiObject);

        }
        return dsiObject;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ACTask.class);

    // List of descriptor names
    protected static final String DSC_VISIBILITY = "visibility";
    protected static final String DSC_BODY = "body";
    protected static final String DSC_OTHER_ASSIGNEES_ID = "otherAssigneesId";
    protected static final String DSC_ASSIGNEE_ID = "assigneeId";
    protected static final String DSC_COMPLETE = "complete";

    private SyncActionWrapper syncCreateEvent(final Issue newGHIssue, final SyncEvent triggerEvent) {
        final Set<PropertyDescriptor> tickedDescriptors = new HashSet<>();
        for (PropertyDescriptor changedDescriptor : triggerEvent.getChangedPropertyDescriptors()) {
            tickedDescriptors.add(changedDescriptor);
            switch (changedDescriptor.getName()) {
            case DSC_OTHER_ASSIGNEES_ID:
            case DSC_ASSIGNEE_ID:
            case DSC_ID:
            case DSC_URL:
                //the ones that we don't have to do anything
            case DSC_CREATED_ON:
            case DSC_UPDATED_ON:
            case DSC_PRIORITY:
            case DSC_UPDATED_BY_ID:
            case DSC_DUE_ON:
                //for now, let's do nothing with the id
                //of who created it
            case DSC_CREATED_BY_ID:
                break;
            case DSC_NAME:
                break;
            case DSC_COMPLETE:
                break;
            case DSC_VISIBILITY:
                break;
            case DSC_BODY:
                break;
            default:
                tickedDescriptors.remove(changedDescriptor); //if we did not fall on any of the above
                //cases, let's remove it from the ticked descriptors

            }
        }

        SyncActionWrapper toReturnActionWrapper = new SyncActionWrapper<GHIssue>() {

            @Override
            public Collection<GHIssue> sync() throws IOException {



                //let's try to find out if we need to create a GHIssue (if we have an ACTaskCategory that
                //has an DSIRepository associated, then we do)
                ACTaskCategory acTaskCategory = getTaskCategory();
                if (acTaskCategory == null || acTaskCategory.getDSIObject() == null) {
                    return Collections.emptySet();
                }

                //now, let's get the repository
                DSIRepository dsiRepository = (DSIRepository) acTaskCategory.getDSIObject();
                GHRepository ghRepository = dsiRepository.getGitHubRepository();

                //the label corresponds to the project name, let's try to retrieve it
                final DSIProject dsiProject = (DSIProject) getProject().getDSIObject(); //depended


                final GHLabel gitHubLabel = dsiProject.getGitHubLabelFor(ghRepository);
                final GHUser repoOwner = ghRepository.getOwner();
                LabelService labelService = new LabelService(MaidRoot.getGitHubClient());

                //let's get the repository
                RepositoryService repositoryService = new RepositoryService(MaidRoot.getGitHubClient());
                Repository repository = repositoryService.getRepository(repoOwner.getLogin(), ghRepository.getName());

                Label label = labelService.getLabel(repository, gitHubLabel.getName());
                newGHIssue.setLabels(Collections.singletonList(label));

                //milestone
                ACMilestone acMilestone = getMilestone();
                final DSIMilestone dsiMilestone = (DSIMilestone) acMilestone.getDSIObject(); //depended upon
                final GHMilestone ghMilestone = dsiMilestone.getGhMilestone();
                MilestoneService milestoneService = new MilestoneService(MaidRoot.getGitHubClient());
                Milestone milestone = milestoneService.getMilestone(repository, ghMilestone.getNumber());
                newGHIssue.setMilestone(milestone);

                newGHIssue.setBodyHtml(getBody());

                newGHIssue.setTitle(getName());

                //TODO assignee

                //let's create the issue
                IssueService issueService = new IssueService(MaidRoot.getGitHubClient());
                Issue newlyCreatedIssue = issueService.createIssue(repository, newGHIssue);

                GHIssue ghProcess = GHIssue.process(newlyCreatedIssue, repository, true);

                //we must add it to the other side of the DSIElement
                DSIIssue dsiIssue = (DSIIssue) getDSIObject();
                dsiIssue.setGhIssue(ghProcess);
                return Collections.singleton(ghProcess);
            }

            @Override
            public Collection<DSIObject> getSyncDependedDSIObjects() {
                Set<DSIObject> dsiObjectsDependedOn = new HashSet<>();
                dsiObjectsDependedOn.add(getProject().getDSIObject());
                dsiObjectsDependedOn.add(getMilestone().getDSIObject());
                return dsiObjectsDependedOn;
            }

            @Override
            public Collection<PropertyDescriptor> getPropertyDescriptorsTicked() {
                return tickedDescriptors;
            }

            @Override
            public SyncEvent getOriginatingSyncEvent() {
                return triggerEvent;
            }

            @Override
            public Set<Class> getSyncDependedTypesOfDSIObjects() {
                Set<Class> classesDependedOn = new HashSet<>();
                classesDependedOn.add(DSIProject.class);
                classesDependedOn.add(DSIMilestone.class);
                classesDependedOn.add(DSIRepository.class);
                return classesDependedOn;
            }
        };



        return toReturnActionWrapper;
    }

    private SyncActionWrapper syncUpdateEvent(final Issue ghIssueToUpdate, final GHIssue ghIssue, final SyncEvent triggerEvent) {
        final Set<PropertyDescriptor> tickedDescriptors = new HashSet<>();
        for (PropertyDescriptor changedDescriptor : triggerEvent.getChangedPropertyDescriptors()) {
            tickedDescriptors.add(changedDescriptor);
            switch (changedDescriptor.getName()) {

            case DSC_OTHER_ASSIGNEES_ID:
                //let us just ignore the other assignees for now
                break;
            case DSC_COMPLETE:
                //let's make the task as completed, or not
                if (getComplete()) {
                    ghIssueToUpdate.setState(GHIssue.STATE_CLOSED);
                } else {
                    ghIssueToUpdate.setState(GHIssue.STATE_OPEN);
                }
                break;
            case DSC_ASSIGNEE_ID:
                //for now let's ignore the assignee TODO
                break;
            case DSC_URL:
                break;
            case DSC_ID:
                ghIssueToUpdate.setId(ghIssue.getId()); //making sure it is the same id
                break;
                //the ones that we don't have to do anything
            case DSC_CREATED_ON:
            case DSC_UPDATED_ON:
            case DSC_PRIORITY:
            case DSC_UPDATED_BY_ID:
            case DSC_DUE_ON:
                break;
                //for now, let's do nothing with the id
                //of who created it
            case DSC_CREATED_BY_ID:
                break;
            case DSC_NAME:
                ghIssueToUpdate.setTitle(getName());
                break;
            case DSC_VISIBILITY:
                //if the visibility is concealed, we wouldn't reach here, let's just make sure that's so
                if (getVisibility() == false)
                    throw new SyncActionError("We are trying to sync a Task that isn't visible");
                break;
            case DSC_BODY:
                ghIssueToUpdate.setBodyHtml(getBody());
                break;
            default:
                tickedDescriptors.remove(changedDescriptor); //if we did not fall on any of the above
                //cases, let's remove it from the ticked descriptors

            }
        }

        SyncActionWrapper toReturnActionWrapper = new SyncActionWrapper() {

            @Override
            public Collection<GHIssue> sync() throws IOException {
                //get the repository
                GHRepository ghRepository = ghIssue.getRepository();
                GHUser ghOwner = ghRepository.getOwner();
                RepositoryService repositoryService = new RepositoryService(MaidRoot.getGitHubClient());
                Repository repository = repositoryService.getRepository(ghOwner.getLogin(), ghRepository.getName());

                //let's edit the issue
                IssueService issueService = new IssueService(MaidRoot.getGitHubClient());
                Issue newlyEditedIssue = issueService.editIssue(repository, ghIssueToUpdate);
                GHIssue processedGHIssue = GHIssue.process(newlyEditedIssue, repository, true);

                //extra check
                if (ObjectUtils.equals(processedGHIssue, ((DSIIssue) getDSIObject()).getGhIssue()) == false)
                    throw new SyncActionError("we did an update and the resulting GHIssue don't match");


                return Collections.singleton(processedGHIssue);
            }

            @Override
            public Collection<DSIObject> getSyncDependedDSIObjects() {
                return Collections.EMPTY_LIST;
            }

            @Override
            public Collection<PropertyDescriptor> getPropertyDescriptorsTicked() {
                return tickedDescriptors;
            }

            @Override
            public SyncEvent getOriginatingSyncEvent() {
                return triggerEvent;
            }

            @Override
            public Set<Class> getSyncDependedTypesOfDSIObjects() {
                return Collections.emptySet();
            }
        };

        return null;
    }

    @Override
    public SyncActionWrapper sync(final SyncEvent syncEvent) {
        SyncActionWrapper syncActionWrapperToReturn = null;
        if (syncEvent.getTypeOfChangeEvent().equals(SyncEvent.TypeOfChangeEvent.CREATE)) {
            //then we should need to create a GHIssue, let's just make sure that's correct
            if (syncEvent.getTargetSyncUniverse().equals(SyncEvent.SyncUniverse.ACTIVE_COLLAB))
                throw new SyncEventIncogruenceBetweenOriginAndDestination("For syncEvent: " + syncEvent.toString());
            Issue newGhIssueToCreate = new Issue();
            syncActionWrapperToReturn = syncCreateEvent(newGhIssueToCreate, syncEvent);

        } else if (syncEvent.getTypeOfChangeEvent().equals(SyncEvent.TypeOfChangeEvent.UPDATE)) {
            //let's retrieve the existing GHIssue and use it to prefill the Issue
            DSIIssue dsiIssue = (DSIIssue) getDSIObject();
            GHIssue ghIssue = dsiIssue.getGhIssue();
            Issue ghIssueToUpdate = new Issue();
            try {
                ghIssue.copyPropertiesTo(ghIssueToUpdate);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | TaskNotVisibleException e) {
                throw new SyncActionError("Error trying to prefill the properties from the GHIssue to the bean.", syncEvent, e);
            }
            syncActionWrapperToReturn = syncUpdateEvent(ghIssueToUpdate, ghIssue, syncEvent);


        } else {
            LOGGER.warn("Read and Delete events not supported yet. " + syncEvent);
            syncActionWrapperToReturn = new EmptySyncActionWrapper(syncEvent);
        }

        return syncActionWrapperToReturn;
    }


}
