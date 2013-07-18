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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MilestoneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.maidSyncher.api.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SynchableObject;
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
import pt.ist.maidSyncher.domain.github.GHObject;
import pt.ist.maidSyncher.domain.github.GHRepository;
import pt.ist.maidSyncher.domain.github.GHUser;
import pt.ist.maidSyncher.domain.sync.EmptySyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncEvent;
import pt.ist.maidSyncher.domain.sync.logs.SyncWarningLog;
import pt.ist.maidSyncher.utils.MiscUtils;

public class ACTask extends ACTask_Base {

    public ACTask() {
        super();
    }

    private Collection<String> processMainAssignee(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) {

        ACUser acUser = ACUser.findById(acTask.getAssigneeId());
        ACUser oldMainAssignee = getMainAssignee();
        setMainAssignee(acUser);
        return !ObjectUtils.equals(acUser, oldMainAssignee) ? Collections.singleton(getPropertyDescriptorNameAndCheckItExists(
                acTask, "assigneeId")) : Collections.EMPTY_SET;
    }

    private Collection<String> processOtherAssignees(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) {

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
        for (ACUser user : getOtherAssigneesSet()) {
            removeOtherAssignees(user);
        }

        for (ACUser user : newOtherAssigneesSet)
            addOtherAssignees(user);
        return somethingChanged ? Collections.singleton(getPropertyDescriptorNameAndCheckItExists(acTask, "otherAssigneesId")) : Collections.EMPTY_SET;
    }

    private Collection<String> processMilestone(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) {
        ACMilestone newMilestone = ACMilestone.findById(acTask.getMilestoneId());
        ACMilestone oldMilestone = getMilestone();
        setMilestone(newMilestone);
        return !ObjectUtils.equals(oldMilestone, newMilestone) ? Collections.singleton(getPropertyDescriptorNameAndCheckItExists(
                acTask, "milestoneId")) : Collections.EMPTY_SET;

    }

    @Override
    public Collection<String> copyPropertiesFrom(Object orig) throws IllegalAccessException, InvocationTargetException,
    NoSuchMethodException {
        Set<String> changedDescriptors = new HashSet<>(super.copyPropertiesFrom(orig));

        pt.ist.maidSyncher.api.activeCollab.ACTask acTask = (pt.ist.maidSyncher.api.activeCollab.ACTask) orig;
        //now let's take care of the milestone, main assignee and other assignees

        changedDescriptors.addAll(processMainAssignee(acTask));

        changedDescriptors.addAll(processOtherAssignees(acTask));

        changedDescriptors.addAll(processMilestone(acTask));

        changedDescriptors.addAll(processCategory(acTask));

        changedDescriptors.addAll(processLabel(acTask));

        changedDescriptors.addAll(processProject(acTask));

        return changedDescriptors;

    }

    private Collection<String> processProject(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) {
        //let's get the projectid
        int projectId = acTask.getProjectId();
        checkArgument(projectId > 0);
        pt.ist.maidSyncher.domain.activeCollab.ACProject acCurrentProject =
                pt.ist.maidSyncher.domain.activeCollab.ACProject.findById(projectId);
        pt.ist.maidSyncher.domain.activeCollab.ACProject acOldProject = getProject();

        if (ObjectUtils.equals(acCurrentProject, acOldProject) == false) {
            setProject(acCurrentProject);
            return Collections.singleton(getPropertyDescriptorNameAndCheckItExists(acTask, DSC_PROJECT_ID));
        } else
            return Collections.emptySet();
    }

    public static ACTask findById(long id) {
        return (ACTask) MiscUtils.findACObjectsById(id, ACTask.class);
    }

    private Collection<String> processLabel(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) {
        ACTaskLabel newTaskLabel = ACTaskLabel.findById(acTask.getLabelId());
        ACTaskLabel oldTaskLabel = getLabel();
        setLabel(newTaskLabel);
        return !ObjectUtils.equals(newTaskLabel, oldTaskLabel) ? Collections.singleton(getPropertyDescriptorNameAndCheckItExists(
                acTask, "labelId")) : Collections.EMPTY_SET;

    }

    private Collection<String> processCategory(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) {
        ACTaskCategory newTaskCategory = ACTaskCategory.findById(acTask.getCategoryId());
        ACTaskCategory oldTaskCategory = getTaskCategory();
        setTaskCategory(newTaskCategory);
        return !ObjectUtils.equals(newTaskCategory, oldTaskCategory) ? Collections
                .singleton(getPropertyDescriptorNameAndCheckItExists(acTask, "categoryId")) : Collections.EMPTY_SET;
    }

    static ACTask process(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) throws TaskNotVisibleException {
        return process(acTask, false);
    }

    static ACTask process(pt.ist.maidSyncher.api.activeCollab.ACTask acTask, boolean skipSync) throws TaskNotVisibleException {
        checkNotNull(acTask);
        //let's check on the visibility
        if (acTask.getVisibility() == false)
            throw new TaskNotVisibleException();
        return (ACTask) findOrCreateAndProccess(acTask, ACTask.class, MaidRoot.getInstance().getAcObjectsSet(), skipSync);
    }

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
        toReturn.addAll(getOtherAssigneesSet());
        toReturn.add(getMainAssignee());

        return toReturn;

    }

    @Override
    public DSIObject getDSIObject() {
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
    public static final String DSC_VISIBILITY = "visibility";
    public static final String DSC_BODY = "body";
    public static final String DSC_OTHER_ASSIGNEES_ID = "otherAssigneesId";
    public static final String DSC_ASSIGNEE_ID = "assigneeId";

    public static final String DSC_MILESTONE_ID = "milestoneId";
    public static final String DSC_CATEGORY_ID = "categoryId";
    public static final String DSC_PROJECT_ID = "projectId";

    private SyncActionWrapper syncCreateEvent(final Issue newGHIssue, final SyncEvent triggerEvent) {
        final Set<String> tickedDescriptors = new HashSet<>();
        for (String changedDescriptor : triggerEvent.getChangedPropertyDescriptorNames().getUnmodifiableList()) {
            tickedDescriptors.add(changedDescriptor);
            switch (changedDescriptor) {
            case DSC_PERMALINK:
            case DSC_OTHER_ASSIGNEES_ID:
            case DSC_ASSIGNEE_ID:
            case DSC_ID:
            case DSC_URL:
            case DSC_PROJECT_ID:
            case DSC_CATEGORY_ID:
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
            case DSC_MILESTONE_ID:
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
            public Set<SynchableObject> sync() throws SyncActionError {

                Set<SynchableObject> changedObjects = new HashSet<>();
                try {
                    //let's try to find out if we need to create a GHIssue (if we have an ACTaskCategory that
                    //has an DSIRepository associated, then we do)
                    ACTaskCategory acTaskCategory = getTaskCategory();
                    if (ACTaskCategory.hasGHSide(acTaskCategory) == false) {
                        return Collections.emptySet();
                    }

                    //now, let's get the repository
                    DSIRepository dsiRepository = (DSIRepository) acTaskCategory.getDSIObject();
                    GHRepository ghRepository = dsiRepository.getGitHubRepository();

                    //the label corresponds to the project name, let's try to retrieve it
                    final DSIProject dsiProject = (DSIProject) getProject().getDSIObject(); //depended

                    syncGHLabelFromACProject(getProject(), ghRepository, newGHIssue);

                    final GHUser repoOwner = ghRepository.getOwner();
//                LabelService labelService = new LabelService(MaidRoot.getGitHubClient());

                    GHObjectWrapper synchedGHMilestoneFromACMilestone =
                            syncGHMilestoneFromACMilestone(getMilestone(), ghRepository, newGHIssue);
                    if (synchedGHMilestoneFromACMilestone != null && synchedGHMilestoneFromACMilestone.wasJustCreated) {
                        changedObjects.add(synchedGHMilestoneFromACMilestone.ghObject);
                    }

                    //TODO #16 - probably we will have to strip the html
                    newGHIssue.setBody(getBody());

                    newGHIssue.setTitle(getName());

                    //TODO assignee

                    //let's create the issue
                    IssueService issueService = new IssueService(MaidRoot.getGitHubClient());
                    Issue newlyCreatedIssue = issueService.createIssue(ghRepository, newGHIssue);

                    GHIssue ghProcess = GHIssue.process(newlyCreatedIssue, ghRepository, true);
                    changedObjects.add(ghProcess);

                    //we must add it to the other side of the DSIElement
                    DSIIssue dsiIssue = (DSIIssue) getDSIObject();
                    dsiIssue.setGhIssue(ghProcess);
                } catch (IOException ex) {
                    throw new SyncActionError(ex, changedObjects);
                }
                return changedObjects;
            }

            @Override
            public Collection<DSIObject> getSyncDependedDSIObjects() {
                Set<DSIObject> dsiObjectsDependedOn = new HashSet<>();
                dsiObjectsDependedOn.add(getProject().getDSIObject());
                if (getMilestone() != null)
                    dsiObjectsDependedOn.add(getMilestone().getDSIObject());
                return dsiObjectsDependedOn;
            }

            @Override
            public Collection<String> getPropertyDescriptorNamesTicked() {
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

    private GHMilestone createSuitableGHMilestone(GHRepository ghRepository, ACMilestone acMilestone) throws IOException {
        MilestoneService milestoneService = new MilestoneService(MaidRoot.getGitHubClient());
        Milestone newMilestone = new Milestone();
        newMilestone.setTitle(acMilestone.getName());
        newMilestone.setDescription(acMilestone.getBody());
        newMilestone.setDueOn(acMilestone.getDueOn() != null ? acMilestone.getDueOn().toDate() : null);
        Milestone createdMilestone = milestoneService.createMilestone(ghRepository, newMilestone);
        GHMilestone processedGhMilestone = GHMilestone.process(createdMilestone, true);
        ghRepository.addMilestones(processedGhMilestone);
        return processedGhMilestone;
    }

    private GHMilestone tryToFindSuitableGHMilestone(GHRepository ghRepository, ACMilestone acMilestone) {
        for (GHMilestone ghMilestone : ghRepository.getMilestonesSet()) {
            if (ObjectUtils.equals(acMilestone.getName(), ghMilestone.getTitle()))
                return ghMilestone;
        }
        return null;
    }

    private GHLabel createSuitableGHLabel(GHRepository ghRepository, pt.ist.maidSyncher.domain.activeCollab.ACProject project)
            throws IOException {
        LabelService labelService = new LabelService(MaidRoot.getInstance().getGitHubClient());
        Label newLabel = new Label();
        newLabel.setName(GHLabel.PROJECT_PREFIX + project.getName());
        Label createdLabel = labelService.createLabel(ghRepository, newLabel);
        return GHLabel.process(createdLabel, ghRepository.getId(), true);
    }

    /**
     * 
     * @param ghRepository
     * @param project
     * @return a GHLabel that is suitable (i.e. its name matches the one needed for the given project), if any was found,
     *         or null otherwise
     */
    private GHLabel tryToFindSuitableGHLabel(GHRepository ghRepository, pt.ist.maidSyncher.domain.activeCollab.ACProject project) {
        for (GHLabel ghLabel : ghRepository.getLabelsDefinedSet()) {
            if ((GHLabel.PROJECT_PREFIX + project.getName()).equalsIgnoreCase(ghLabel.getName())) {
                return ghLabel;
            }
        }
        return null;
    }

    private SyncActionWrapper syncUpdateEvent(final SyncEvent triggerEvent) {
        final Set<String> tickedDescriptors = new HashSet<>();
        for (String changedDescriptor : triggerEvent.getChangedPropertyDescriptorNames().getUnmodifiableList()) {
            tickedDescriptors.add(changedDescriptor);
            switch (changedDescriptor) {

            case DSC_OTHER_ASSIGNEES_ID:
                //let us just ignore the other assignees for now
                break;
            case DSC_COMPLETE:
                break;
            case DSC_ASSIGNEE_ID:
                //for now let's ignore the assignee TODO
                break;
            case DSC_URL:
                break;
            case DSC_ID:
                //that shouldn't have happened!!
                new SyncWarningLog(MaidRoot.getInstance().getCurrentSyncLog(),
                        "Id of ACTask changed!!. It shouldn't have happened, oh well. SyncEvent: " + triggerEvent);
                break;
                //the ones that we don't have to do anything
            case DSC_PERMALINK:
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
                break;
            case DSC_VISIBILITY:
                //if the visibility is concealed, we wouldn't reach here, let's just make sure that's so
                if (getVisibility() == false)
                    throw new SyncActionError("We are trying to sync a Task that isn't visible");
                break;
            case DSC_BODY:
                //TODO #16 probably strip the HTML from the getBody
                break;
            case DSC_PROJECT_ID:
                //the project changed, thus we must change the label from the GH side
                //but let's do it on the sync (giving a chance for the Labels and etc
                //to be already synched)

                break;
            case DSC_MILESTONE_ID:
                break;
            case DSC_CATEGORY_ID:
                break;

            default:
                tickedDescriptors.remove(changedDescriptor); //if we did not fall on any of the above
                //cases, let's remove it from the ticked descriptors

            }
        }

        final boolean completeChanged = tickedDescriptors.contains(DSC_COMPLETE);
        final boolean projectChanged = tickedDescriptors.contains(DSC_PROJECT_ID);
        final boolean milestoneChanged = tickedDescriptors.contains(DSC_MILESTONE_ID);
        final boolean taskCategoryChanged = tickedDescriptors.contains(DSC_CATEGORY_ID);
        final boolean taskNameChanged = tickedDescriptors.contains(DSC_NAME);

        final boolean taskBodyChanged = tickedDescriptors.contains(DSC_BODY);

        return new SyncActionWrapper() {

            @Override
            public Set<SynchableObject> sync() throws SyncActionError {
                Issue ghIssueToUpdate = null;
                DSIIssue dsiIssue = (DSIIssue) getDSIObject();
                GHIssue ghIssue = dsiIssue.getGhIssue();

                Set<SynchableObject> changedObjects = new HashSet<SynchableObject>();

                try {

                    ACTaskCategory taskCategory = getTaskCategory();
                    GHRepository newGHRepository = null;
                    //setting the newGHRepository
                    if (taskCategory != null) {
                        DSIRepository dsiRepository = (DSIRepository) taskCategory.getDSIObject();
                        newGHRepository = dsiRepository.getGitHubRepository();
                    } else {
                        if (getProject().getDsiRepositoryFromDefaultProject() != null) {
                            newGHRepository = getProject().getDsiRepositoryFromDefaultProject().getGitHubRepository();
                        } else {
                            newGHRepository = null;
                        }
                    }

                    GHRepository oldGHRepository =
                            getDSIObject() == null || ((DSIIssue) getDSIObject()).getGhIssue() == null ? null : ((DSIIssue) getDSIObject())
                                    .getGhIssue().getRepository();
                    if ((taskCategoryChanged && ACTaskCategory.hasGHSide(getTaskCategory()))
                            && !ObjectUtils.equals(newGHRepository, oldGHRepository)) {

                        //as the overriden method of the copyPropertiesTo
                        //already takes care of the labels

                        Issue ghOldIssueToUpdate = null;
                        Issue newIssue = null;
                        if (ghIssue != null) {
                            ghOldIssueToUpdate = ghIssue.getNewPrefilledIssue(null);

                        }
                        newIssue = prefillIssue(newGHRepository, changedObjects);

                        GHIssue newGhCreatedIssue =
                                updateOrCreateIssueIfNotNull(newGHRepository, changedObjects, ghOldIssueToUpdate == null,
                                newIssue);

                        dsiIssue.setGhIssue(newGhCreatedIssue);

                        //if we have an old issue, let's mark it as moved &
                        //do all the needed changes
                        if (ghOldIssueToUpdate != null) {
                            //adding the deleted label to the old issue
                            Label labelDeleted = new Label();
                            labelDeleted.setName(GHLabel.DELETED_LABEL_NAME);

                            List<Label> labelsToUseInTheOldIssue = addLabelsToUse(ghOldIssueToUpdate.getLabels(), labelDeleted);
                            ghOldIssueToUpdate.setLabels(labelsToUseInTheOldIssue);

                            //let's close the old issue
                            ghOldIssueToUpdate.setState(GHIssue.STATE_CLOSED);

                            //and add into the description what happened to it
                            String oldIssueBody = ghOldIssueToUpdate.getBody();
                            String newBodyForIssue = applyMovedTo(oldIssueBody, newGhCreatedIssue);
                            ghOldIssueToUpdate.setBody(newBodyForIssue);

                            //let's 'commit' the old issue changes

                            IssueService issueService = new IssueService(MaidRoot.getGitHubClient());
                            GHIssue oldGHIssue =
                                    GHIssue.process(issueService.editIssue(oldGHRepository, ghOldIssueToUpdate), oldGHRepository,
                                            true);
                            changedObjects.add(oldGHIssue);
                        }
                        //now let's do the same for each subtask
                        for (ACSubTask acSubTask : getSubTasksSet()) {
                            //let's migrate this
                            if (acSubTask.getDsiObjectSubTask().getParentIssue().getGhIssue() == null)
                                acSubTask.getDsiObjectSubTask().getParentIssue().setGhIssue(newGhCreatedIssue);
                            processGHIssueFromACSubTaskMigration(newGHRepository, acSubTask, changedObjects);
                        }

                    } else {
                        updateIssueSimpleFieldsIfNeccessary(ghIssueToUpdate, ghIssue, oldGHRepository, changedObjects, false);

                    }

//                //extra check
//                if (ObjectUtils.equals(processedGHIssue, ((DSIIssue) getDSIObject()).getGhIssue()) == false)
//                    throw new SyncActionError("we did an update and the resulting GHIssue don't match");

                } catch (IOException ex) {
                    throw new SyncActionError(ex, changedObjects);
                }

                return changedObjects;
            }

            private GHIssue updateIssueSimpleFieldsIfNeccessary(Issue ghIssueToUpdate, GHIssue ghIssue,
                    GHRepository ghRepository, Set<SynchableObject> changedObjects, boolean createInsteadOfEdit)
                            throws IOException {
                Issue issue = ghIssueToUpdate;
                if (ghIssue != null) {
                    if (taskNameChanged) {
                        issue = ghIssue.getNewPrefilledIssue(issue);
                        issue.setTitle(getName());
                    }
                    if (taskBodyChanged) {
                        issue = ghIssue.getNewPrefilledIssue(issue);
                        issue.setBody(getBody());
                    }
                    if (completeChanged) {
                        issue = ghIssue.getNewPrefilledIssue(issue);
                        //let's make the task as completed, or not
                        if (getComplete()) {
                            issue.setState(GHIssue.STATE_CLOSED);
                        } else {
                            issue.setState(GHIssue.STATE_OPEN);
                        }
                    }

                    if (projectChanged) {
                        //let's try to find the GHLabel associated with this one
                        //and if it doesn't exist, create it
                        issue = ghIssue.getNewPrefilledIssue(issue);

                        syncGHLabelFromACProject(getProject(), ghRepository, issue);

                    }
                    if (milestoneChanged) {

                        issue = ghIssue.getNewPrefilledIssue(issue);
                        syncGHMilestoneFromACMilestone(getMilestone(), ghRepository, issue);
                    }
                }

                return updateOrCreateIssueIfNotNull(ghRepository, changedObjects, createInsteadOfEdit, issue);
            }

            private GHIssue updateOrCreateIssueIfNotNull(GHRepository ghRepository, Set<SynchableObject> changedObjects,
                    boolean createInsteadOfEdit, Issue issueToCreate) throws IOException {
                //let's edit the issue, if we have to
                if (issueToCreate != null) {

                    IssueService issueService = new IssueService(MaidRoot.getGitHubClient());
                    Issue changedIssue = null;
                    if (createInsteadOfEdit) {
                        changedIssue = issueService.createIssue(ghRepository, issueToCreate);
                    } else {
                        changedIssue = issueService.editIssue(ghRepository, issueToCreate);

                    }
                    GHIssue processedGHIssue = GHIssue.process(changedIssue, ghRepository, true);
                    changedObjects.add(processedGHIssue);
                    return processedGHIssue;
                }
                return null;
            }

            private void processGHIssueFromACSubTaskMigration(GHRepository newGHRepository, ACSubTask acSubTask,
                    Set<SynchableObject> changedObjects) throws IOException {
                //let's get the old issue based on the current one
                Issue oldIssue = new Issue();

                //let's create the new blank Issue
                Issue newIssue = new Issue();

                GHIssue subTaskGHIssue = null;

                //copy everything from the old one, if it exists
                if (acSubTask.getDsiObjectSubTask() != null && acSubTask.getDsiObjectSubTask().getGhIssue() != null) {
                    subTaskGHIssue = acSubTask.getDsiObjectSubTask().getGhIssue();
                    try {
                        subTaskGHIssue.copyPropertiesTo(newIssue);
                        subTaskGHIssue.copyPropertiesTo(oldIssue);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | TaskNotVisibleException e) {
                        throw new SyncActionError("Error trying to prefill the properties from the GHIssue to the bean.", e);
                    }
                } else {
                    //the old one doesn't exist, let's create the GHSide
                    acSubTask.createGHIssueForSubTask(changedObjects, (ACTask) triggerEvent.getOriginObject(), getTaskCategory());
                    return;
                }

                //taking care of the eventual milestone
                GHObjectWrapper syncGHMilestoneFromACMilestone =
                        syncGHMilestoneFromACMilestone(getMilestone(), newGHRepository, newIssue);

                IssueService issueService = new IssueService(MaidRoot.getGitHubClient());
                GHIssue newGhCreatedIssue =
                        GHIssue.process(issueService.createIssue(newGHRepository, newIssue), newGHRepository, true);

                DSIIssue dsiIssue = (DSIIssue) getDSIObject();
                dsiIssue.setGhIssue(newGhCreatedIssue);

                //adding the deleted label to the old issue
                Label labelDeleted = new Label();
                labelDeleted.setName(GHLabel.DELETED_LABEL_NAME);

                List<Label> labelsToUseInTheOldIssue = addLabelsToUse(oldIssue.getLabels(), labelDeleted);
                oldIssue.setLabels(labelsToUseInTheOldIssue);

                //let's close the old issue
                oldIssue.setState(GHIssue.STATE_CLOSED);

                //and add into the description what happened to it
                String oldIssueBody = oldIssue.getBody();
                String newBodyForIssue = applyMovedTo(oldIssueBody, newGhCreatedIssue);
                oldIssue.setBody(newBodyForIssue);
                //let's close the oldIssue
                changedObjects.add(GHIssue.process(issueService.editIssue(subTaskGHIssue.getRepository(), oldIssue),
                        subTaskGHIssue.getRepository(), true));
                return;

            }

            @Override
            public Collection<DSIObject> getSyncDependedDSIObjects() {
                return Collections.EMPTY_LIST;
            }

            @Override
            public Collection getPropertyDescriptorNamesTicked() {
                return tickedDescriptors;
            }

            @Override
            public SyncEvent getOriginatingSyncEvent() {
                return triggerEvent;
            }

            @Override
            public Set<Class> getSyncDependedTypesOfDSIObjects() {
                Set<Class> dependedOnObjects = new HashSet();
                dependedOnObjects.add(ACTaskCategory.class);
                dependedOnObjects.add(ACProject.class);
                return dependedOnObjects;
            }
        };

    }

    static class GHObjectWrapper {
        public final GHObject ghObject;
        public final boolean wasJustCreated;

        public GHObjectWrapper(GHObject ghObject, boolean wasRecentlyCreated) {
            this.ghObject = ghObject;
            this.wasJustCreated = wasRecentlyCreated;
        }
    }

    private List<Label> addLabelsToUse(List<Label> currentlyUsedLabels, Label... labelsToAdd) {
        List<Label> labelsToReturn = null;
        if (currentlyUsedLabels == null) {
            labelsToReturn = new ArrayList<Label>();
        } else {
            labelsToReturn = currentlyUsedLabels;
        }
        for (Label labelToAdd : labelsToAdd) {
            labelsToReturn.add(labelToAdd);
        }
        return labelsToReturn;
    }

    GHObjectWrapper syncGHLabelFromACProject(pt.ist.maidSyncher.domain.activeCollab.ACProject acProject,
            GHRepository ghRepository, Issue issueToUpdate) throws IOException {

        boolean wasJustCreated = false;
        DSIProject dsiProject = (DSIProject) acProject.getDSIObject();
        GHLabel gitHubLabel = dsiProject.getGitHubLabelFor(ghRepository);
        if (gitHubLabel == null) {
            gitHubLabel = tryToFindSuitableGHLabel(ghRepository, acProject);
            if (gitHubLabel == null) {
                gitHubLabel = createSuitableGHLabel(ghRepository, acProject);
                wasJustCreated = true;

            }
        }

        Label newLabel = new Label();
        newLabel.setName(gitHubLabel.getName());
        List<Label> labelsToUse = addLabelsToUse(issueToUpdate.getLabels(), newLabel);

        issueToUpdate.setLabels(labelsToUse);

        return new GHObjectWrapper(gitHubLabel, wasJustCreated);

    }

    /**
     * 
     * @param acMilestone the acMilestone to process
     * @param ghRepository the repository where to place the new Milestone
     * @param ghIssueToUpdate the {@link Issue} object to edit/create
     * @return a {@link GHMilestoneWrapper} with the {@link GHMilestone} reused/created
     *         creates/reuses a milestone that it might find on the given repository, wich is returned in
     *         a {@link GHMilestoneWrapper}, and updates it on the given ghIssueToUpdate
     * 
     * @throws IOException if something went wrong with the creation of a new Milestone
     *             Creates or reuses a GHMilestone if there is an associated acMilestone
     */
    GHObjectWrapper syncGHMilestoneFromACMilestone(ACMilestone acMilestone, GHRepository ghRepository, Issue ghIssueToUpdate)
            throws IOException {
        if (acMilestone != null) {
            checkNotNull(ghIssueToUpdate);
            checkNotNull(ghRepository);
            boolean wasCreatedANewMilestone = false;

            final DSIMilestone dsiMilestone = (DSIMilestone) acMilestone.getDSIObject(); //depended upon
            GHMilestone ghMilestone = dsiMilestone.getGhMilestone(ghRepository);
            if (ghMilestone == null) {
                //we must reuse/create it
                ghMilestone = tryToFindSuitableGHMilestone(ghRepository, acMilestone);
                if (ghMilestone == null) {
                    ghMilestone = createSuitableGHMilestone(ghRepository, acMilestone);
                    wasCreatedANewMilestone = true;
                }
            }

            //we don't need the milestoneService, just the number
            Milestone milestone = new Milestone();
            milestone.setNumber(ghMilestone.getNumber());
            ghIssueToUpdate.setMilestone(milestone);

            return new GHObjectWrapper(ghMilestone, wasCreatedANewMilestone);
        }
        return null;

    }

    String applyMovedTo(String oldIssueBody, GHIssue newGhCreatedIssue) {
        String newString = null;
        if (oldIssueBody == null) {
            newString = "";
        } else {
            newString = oldIssueBody;
        }
        return GHIssue.MOVED_TO_PREFIX + newGhCreatedIssue.getRepository().generateId() + "#" + newGhCreatedIssue.getNumber()
                + newString;

    }

    Issue prefillIssue(GHRepository targetRepository, Set<SynchableObject> changedObjects) {
        Issue issue = new Issue();
        issue.setBody(getBody());
        GHLabel gitHubLabelForProject = ((DSIProject) getProject().getDSIObject()).getGitHubLabelFor(targetRepository);
        if (gitHubLabelForProject != null) {
            Label label = new Label();
            try {
                gitHubLabelForProject.copyPropertiesTo(label);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | TaskNotVisibleException e) {
                throw new SyncActionError(e);
            }
            List<Label> singletonLabels = Collections.singletonList(label);
            issue.setLabels(singletonLabels);
        }
        if (getComplete() == null) {
            issue.setState(GHIssue.STATE_OPEN);
        } else {
            issue.setState(getComplete() ? GHIssue.STATE_CLOSED : GHIssue.STATE_OPEN);
        }

        issue.setTitle(getName());

        try {
            GHObjectWrapper synchedGHMilestoneFromACMilestone =
                    syncGHMilestoneFromACMilestone(getMilestone(), targetRepository, issue);
            if (synchedGHMilestoneFromACMilestone != null && synchedGHMilestoneFromACMilestone.wasJustCreated)
                changedObjects.add(synchedGHMilestoneFromACMilestone.ghObject);
        } catch (IOException e) {
            throw new SyncActionError(e);
        }

        return issue;
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
            syncActionWrapperToReturn = syncUpdateEvent(syncEvent);

        } else {
            LOGGER.warn("Read and Delete events not supported yet. " + syncEvent);
            syncActionWrapperToReturn = new EmptySyncActionWrapper(syncEvent);
        }

        return syncActionWrapperToReturn;
    }

}
