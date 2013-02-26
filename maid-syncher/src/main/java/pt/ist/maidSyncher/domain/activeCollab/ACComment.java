package pt.ist.maidSyncher.domain.activeCollab;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.Set;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.api.activeCollab.ACTask;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.activeCollab.exceptions.TaskNotVisibleException;

public class ACComment extends ACComment_Base {

    public  ACComment() {
        super();
    }

    private static ACComment process(pt.ist.maidSyncher.api.activeCollab.ACComment acComment) {
        checkNotNull(acComment);
        return (ACComment) findOrCreateAndProccess(acComment, ACComment.class, MaidRoot.getInstance().getAcObjects());
    }

    @Service
    public static void process(Set<pt.ist.maidSyncher.api.activeCollab.ACComment> acComments, ACTask acTask) {
        pt.ist.maidSyncher.domain.activeCollab.ACTask task;
        try {
            task = pt.ist.maidSyncher.domain.activeCollab.ACTask.process(acTask);
        } catch (TaskNotVisibleException e) {
            return;
        }

        //let's proccess all of the comments now

        Set<ACComment> acOldComments = new HashSet<ACComment>(task.getComments());

        Set<ACComment> acNewComments = new HashSet<ACComment>();
        for (pt.ist.maidSyncher.api.activeCollab.ACComment acComment : acComments) {
            acNewComments.add(process(acComment));
        }

        //TODO check, and sync, if there were deleted comments

        for (ACComment acComment : task.getComments()) {
            task.removeComments(acComment);
        }

        for (ACComment acComment : acNewComments) {
            task.addComments(acComment);
        }

    }

    @Override
    public void sync(Object objectThatTriggeredTheSync) {
        // TODO Auto-generated method stub

    }

}
