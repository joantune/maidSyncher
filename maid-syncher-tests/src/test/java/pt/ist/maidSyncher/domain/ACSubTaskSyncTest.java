/**
 * 
 */
package pt.ist.maidSyncher.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.service.IssueService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.maidSyncher.domain.activeCollab.ACSubTask;
import pt.ist.maidSyncher.domain.github.GHIssue;
import pt.ist.maidSyncher.domain.github.GHLabel;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncEvent;
import pt.ist.maidSyncher.domain.sync.SyncEvent.TypeOfChangeEvent;
import pt.ist.maidSyncher.domain.test.utils.TestUtils;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 8 de Mai de 2013
 * 
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ACSubTaskSyncTest extends ACTaskSyncTest {

//    @Before
//    @Atomic(mode = TxMode.WRITE)
//    public void initialize() throws IOException {
//        TestUtils.clearInstancesWithRoot();
//        init();
//
//    }

    ACSubTask acSubTask;

    @Test
    @Atomic(mode = TxMode.WRITE)
    public void createACSubTaskWithAllFields() throws IOException {
        //let's create a subTask without the GH side
        acSubTask = initializeSubTaskAndCorrespondingGHIssue(acTask, AC_SUB_TASK_ONE_NAME, ghRepository, false);
        initializeACMilestone();
        initializeAssociatedGHIssue();

        //setup the stubs
        when(gitHubClient.post(Mockito.anyString(), Mockito.anyObject(), Mockito.eq(Milestone.class))).then(
                new Answer<Milestone>() {

                    @Override
                    public Milestone answer(InvocationOnMock invocation) throws Throwable {
                        Milestone milestone = (Milestone) invocation.getArguments()[1];
                        if (milestone.getUrl() == null)
                            milestone.setUrl("http://phonyUrl.com/");
                        milestone.setNumber(GH_MILESTONE_NUMBER);
                        return milestone;
                    }

                });

        when(gitHubClient.post(Mockito.anyString(), Mockito.any(Object.class), Mockito.eq(Label.class))).thenAnswer(
                new Answer<Label>() {

                    @Override
                    public Label answer(InvocationOnMock invocation) throws Throwable {
                        //let's get the label to return the label :)
                        Label labelArgument = (Label) invocation.getArguments()[1];
                        when(labelMock.getName()).thenReturn(labelArgument.getName());
                        return labelMock;
                    }

                });
        SyncEvent createSubTaskEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, acSubTask,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(pt.ist.maidSyncher.api.activeCollab.ACSubTask.class)));

        SyncActionWrapper sync = acSubTask.sync(createSubTaskEvent);
        sync.sync();

        //verifications

        //the creation of the issue associated with this subtask
        verify(gitHubClient).post(stringPostUriCaptor.capture(), postCaptor.capture(), Mockito.eq(Issue.class));

        Map<Object, Object> issueMap = postCaptor.getValue();

        assertEquals(AC_SUB_TASK_ONE_NAME, issueMap.get(IssueService.FIELD_TITLE));

        String body = (String) issueMap.get(IssueService.FIELD_BODY);

        assertTrue(body.startsWith(GHIssue.SUB_TASK_BODY_PREFIX));

        //we did a creation of an issue on the right repository
        assertTrue(stringPostUriCaptor.getValue().contains(ghRepository.generateId()));

        //we have the correct milestone set on the issue
        String milestoneNumberString = (String) issueMap.get(IssueService.FILTER_MILESTONE);
        assertEquals(String.valueOf(GH_MILESTONE_NUMBER), milestoneNumberString);


        //seen that we didn't had it first, we must had a creation of it
        verify(gitHubClient).post(Mockito.anyString(), milestoneCaptor.capture(), Mockito.eq(Milestone.class));

        //and the name must be equal
        Milestone milestoneCaptured = milestoneCaptor.getValue();
        assertEquals(GH_MILESTONE_TITLE, milestoneCaptured.getTitle());

        //the label must be the aproppriate (i.e. the one from the ACProject where the
        //parent task is)
        List<String> labelNames = (List<String>) issueMap.get(IssueService.FILTER_LABELS);
        assertTrue(labelNames.contains(GHLabel.PROJECT_PREFIX + AC_PROJECT_NAME));

    }

    @Test
    @Atomic(mode = TxMode.WRITE)
    public void updateWithAllDescriptorChange() throws IOException {
        //let's create a subTask without the GH side
        acSubTask = initializeSubTaskAndCorrespondingGHIssue(acTask, AC_SUB_TASK_ONE_NAME, ghRepository, true);
        initializeACMilestone(); //shouldn't make an impact
        initializeAssociatedGHIssue(); //needed

        SyncEvent updateSubTaskEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.UPDATE, acSubTask,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(pt.ist.maidSyncher.api.activeCollab.ACSubTask.class)));

        SyncActionWrapper sync = acSubTask.sync(updateSubTaskEvent);
        sync.sync();

        //verifications

        verify(gitHubClient).post(stringPostUriCaptor.capture(), postCaptor.capture(), Mockito.eq(Issue.class));

        Map<Object, Object> issueMap = postCaptor.getValue();

        assertEquals(AC_SUB_TASK_ONE_NAME, issueMap.get(IssueService.FIELD_TITLE));

        //we did an edit of an issue on the right repository
        assertTrue(stringPostUriCaptor.getValue().contains(ghRepository.generateId()));

        //for the right GHIssue
        assertTrue(stringPostUriCaptor.getValue().contains(String.valueOf(GH_SUBTASK_ONE_ISSUE_NUMBER)));

    }

}
