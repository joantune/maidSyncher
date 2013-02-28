package pt.ist.maidSyncher.domain.github;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.maidSyncher.domain.MaidRoot;

public class GHIssue extends GHIssue_Base {

    public GHIssue() {
        super();
        MaidRoot.getInstance().addGhIssues(this);
    }

    static GHIssue process(Issue issue) {
        checkNotNull(issue);
        MaidRoot maidRoot = MaidRoot.getInstance();
        return (GHIssue) findOrCreateAndProccess(issue, GHIssue.class, maidRoot.getGhIssues());
    }

    @Service
    public static GHIssue process(Issue issue, Repository repository) {
        checkNotNull(repository);
        //let's first take care of the issue, and then assign it the repository
        GHIssue ghIssue = process(issue);
        GHRepository ghRepository = GHRepository.process(repository);

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
    public void sync(Object objectThatTriggeredTheSync, Collection<PropertyDescriptor> changedDescriptors) {
        // TODO Auto-generated method stub

    }

}
