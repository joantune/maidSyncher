package pt.ist.maidSyncher.domain.github;

import static com.google.common.base.Preconditions.checkNotNull;
import jvstm.cps.ConsistencyPredicate;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.joda.time.LocalTime;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SyncEvent;
import pt.ist.maidSyncher.domain.dsi.DSIObject;

public class GHComment extends GHComment_Base {

    public GHComment() {
        super();
        MaidRoot.getInstance().addGhComments(this);
    }

    //TODO receive a list of comments, so that we can detect deletions of comments

    @Service
    public static GHComment process(Comment comment, Issue issue) {
        checkNotNull(comment);
        checkNotNull(issue);

        MaidRoot maidRoot = MaidRoot.getInstance();

        GHComment ghComment = (GHComment) findOrCreateAndProccess(comment, GHComment.class, maidRoot.getGhComments());

        //now let's attach it to the issue
        GHIssue ghIssue = GHIssue.process(issue);

        ghIssue.addComments(ghComment);

        return ghComment;
    }

    @Override
    public void sync(SyncEvent syncEvent) {
        // TODO Auto-generated method stub

    }

    @ConsistencyPredicate
    private boolean checkMultiplicityDsiObject() {

        //we must have only one of them (because this is either a comment, logged time, or sub task) or zero
        boolean hasDsiObject = false;
        if (hasDsiObjectComment())
            hasDsiObject = true;
        if (hasDsiObjectLoggedTime()) {
            if (hasDsiObject)
                return false;
            hasDsiObject = true;
        }

        if (hasDsiObjectSubTask()) {

            if (hasDsiObject)
                return false;
            hasDsiObject = true;
        }
        return true; //we also allow having no dsi object (which should be temporary)
    }

    @Override
    protected DSIObject getDSIObject() {
        if (hasDsiObjectComment())
            return getDsiObjectComment();
        if (hasDsiObjectLoggedTime())
            return getDsiObjectLoggedTime();
        if (hasDsiObjectSubTask())
            return getDsiObjectSubTask();
        return null;
    }

    @Override
    public DSIObject findOrCreateDSIObject() {
        DSIObject dsiObject = getDSIObject();
        if (dsiObject == null)
            //TODO
            return null;
        return null;
    }

    @Override
    public LocalTime getUpdatedAtDate() {
        return getUpdatedAtDate() == null ? getCreatedAt() : getUpdatedAt();
    }

}
