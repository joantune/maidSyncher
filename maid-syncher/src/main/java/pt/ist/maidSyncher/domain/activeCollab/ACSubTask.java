package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.Set;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.api.activeCollab.ACTask;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;


public class ACSubTask extends ACSubTask_Base {

    public  ACSubTask() {
        super();
    }

    @Service
    private static ACSubTask process(pt.ist.maidSyncher.api.activeCollab.ACSubTask acSubTask) {
        checkNotNull(acSubTask);
        return (ACSubTask) findOrCreateAndProccess(acSubTask, ACSubTask.class, MaidRoot.getInstance().getAcObjects());
    }

    @Service
    public static void process(Set<pt.ist.maidSyncher.api.activeCollab.ACSubTask> acSubTasks, ACTask acTask) {
        pt.ist.maidSyncher.domain.activeCollab.ACTask acDomainTask;
        try {
            acDomainTask = pt.ist.maidSyncher.domain.activeCollab.ACTask.process(acTask);
        } catch (TaskNotVisibleException e) {
            return;
        }

        //let's get all of the sub tasks
        Set<ACSubTask> newSubTaskSet = new HashSet<>();

        Set<ACSubTask> oldSubTaskSet = new HashSet<>(acDomainTask.getSubTasks());
        for (pt.ist.maidSyncher.api.activeCollab.ACSubTask subTask : acSubTasks) {
            ACSubTask domainSubTask = process(subTask);
            newSubTaskSet.add(domainSubTask);
        }

        //ok, now let's remove all of the old ones and add the new ones TODO sync if changes were made
        for (ACSubTask oldSubTask : acDomainTask.getSubTasks()) {
            acDomainTask.removeSubTasks(oldSubTask);
        }

        for (ACSubTask newSubTask : newSubTaskSet) {
            acDomainTask.addSubTasks(newSubTask);
        }

    }

}
