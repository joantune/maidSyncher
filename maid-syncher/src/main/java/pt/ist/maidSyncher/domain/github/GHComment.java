package pt.ist.maidSyncher.domain.github;

import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.PropertyDescriptor;
import java.util.Collection;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.domain.MaidRoot;

public class GHComment extends GHComment_Base {

    public  GHComment() {
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
    public void sync(Object objectThatTriggeredTheSync, Collection<PropertyDescriptor> changedDescriptors) {
        // TODO Auto-generated method stub
        
    }

}
