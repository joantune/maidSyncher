package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.api.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;
import pt.ist.maidSyncher.utils.MiscUtils;

public class ACTask extends ACTask_Base {

    public ACTask() {
        super();
    }

    private boolean processMainAssignee(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) {

        ACUser acUser = ACUser.findById(acTask.getAssigneeId());
        ACUser oldMainAssignee = getMainAssignee();
        setMainAssignee(acUser);
        return !ObjectUtils.equals(acUser, oldMainAssignee);
    }

    private boolean processOtherAssignees(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) {

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
        return somethingChanged;
    }

    private boolean processMilestone(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) {
        ACMilestone newMilestone = ACMilestone.findById(acTask.getMilestoneId());
        ACMilestone oldMilestone = getMilestone();
        setMilestone(newMilestone);
        return !ObjectUtils.equals(oldMilestone, newMilestone);

    }

    @Override
    public boolean copyPropertiesFrom(Object orig) throws IllegalAccessException, InvocationTargetException,
    NoSuchMethodException {
        boolean somethingChanged = super.copyPropertiesFrom(orig);

        pt.ist.maidSyncher.api.activeCollab.ACTask acTask = (pt.ist.maidSyncher.api.activeCollab.ACTask) orig;
        //now let's take care of the milestone, main assignee and other assignees

        somethingChanged = processMainAssignee(acTask);

        somethingChanged = processOtherAssignees(acTask);

        somethingChanged = processMilestone(acTask);

        somethingChanged = processCategory(acTask);

        somethingChanged = processLabel(acTask);


        return somethingChanged;

    }

    public static ACTask findById(long id) {
        return (ACTask) MiscUtils.findACObjectsById(id, ACTask.class);
    }

    private boolean processLabel(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) {
        ACTaskLabel newTaskLabel = ACTaskLabel.findById(acTask.getLabelId());
        ACTaskLabel oldTaskLabel = getLabel();
        setLabel(newTaskLabel);
        return !ObjectUtils.equals(newTaskLabel, oldTaskLabel);

    }

    private boolean processCategory(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) {
        ACTaskCategory newTaskCategory = ACTaskCategory.findById(acTask.getCategoryId());
        ACTaskCategory oldTaskCategory = getTaskCategory();
        setTaskCategory(newTaskCategory);
        return !ObjectUtils.equals(newTaskCategory, oldTaskCategory);
    }

    @Service
    static ACTask process(pt.ist.maidSyncher.api.activeCollab.ACTask acTask) throws TaskNotVisibleException {
        checkNotNull(acTask);
        //let's check on the visibility
        if (acTask.getVisibility() == false)
            throw new TaskNotVisibleException();
        return (ACTask) findOrCreateAndProccess(acTask, ACTask.class, MaidRoot.getInstance().getAcObjects());
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

    public Set<ACUser> getAssignees() {
        HashSet<ACUser> toReturn = new HashSet<ACUser>();
        toReturn.addAll(getOtherAssignees());
        toReturn.add(getMainAssignee());

        return toReturn;

    }

}
