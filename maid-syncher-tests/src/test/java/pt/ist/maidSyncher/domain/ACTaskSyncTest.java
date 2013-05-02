/**
 * 
 */
package pt.ist.maidSyncher.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.maidSyncher.domain.SyncEvent.TypeOfChangeEvent;
import pt.ist.maidSyncher.domain.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.activeCollab.ACTask;
import pt.ist.maidSyncher.domain.activeCollab.ACTaskCategory;
import pt.ist.maidSyncher.domain.dsi.DSIIssue;
import pt.ist.maidSyncher.domain.dsi.DSIMilestone;
import pt.ist.maidSyncher.domain.dsi.DSIProject;
import pt.ist.maidSyncher.domain.dsi.DSIRepository;
import pt.ist.maidSyncher.domain.github.GHLabel;
import pt.ist.maidSyncher.domain.github.GHMilestone;
import pt.ist.maidSyncher.domain.github.GHRepository;
import pt.ist.maidSyncher.domain.github.GHUser;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.test.utils.TestUtils;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 29 de Abr de 2013
 * 
 *         Tests the synching {@link ACTask#sync(SyncEvent)} method/functionality
 */
@RunWith(MockitoJUnitRunner.class)
public class ACTaskSyncTest {

    private static final String HTTP_LABEL_PHONY_URL = "http://label/phony/url";

    private static final String GH_REP_OWNER_LOGIN = "owner";

    ACTask acTask;

    private final static String AC_TASK_NAME = "ac task name";
    private final static String AC_TASK_BODY = "ac task body";

    private static final String GH_REPOSITORY_NAME = "testRepo";

    DSIMilestone dsiMilestone;

    ACTaskCategory acTaskCategory;

    GHRepository ghRepository;

    private final static String AC_TASK_REPOSITORY_NAME = ACTaskCategory.REPOSITORY_PREFIX + GH_REPOSITORY_NAME;

    private final static long GH_REPOSITORY_ID = 3;

    private final static String GH_REPOSITORY_URL = "http://smthing/repos/" + GH_REPOSITORY_ID;

    private static final String AC_PROJECT_NAME = "projectName";

    DSIRepository dsiRepository;

    ACProject acProject;

    GHLabel ghLabel;

    DSIProject dsiProject;

    DSIIssue dsiIssue;

    GHMilestone ghMilestone;

    private void initializeTaskCategoryRepositoryAndACProject() {

        acTaskCategory = new ACTaskCategory();
        acTaskCategory.setName(AC_TASK_REPOSITORY_NAME);
        dsiRepository = new DSIRepository();

        GHUser repOwner = new GHUser();
        repOwner.setLogin(GH_REP_OWNER_LOGIN);
        ghRepository = new GHRepository();
        ghRepository.setId(GH_REPOSITORY_ID);
        ghRepository.setOwner(repOwner);

        acTaskCategory.setDsiObjectRepository(dsiRepository);
        ghRepository.setName(GH_REPOSITORY_NAME);
        ghRepository.setUrl(GH_REPOSITORY_URL);
        dsiRepository.setGitHubRepository(ghRepository);

        acProject = new ACProject();
        acTaskCategory.setProject(acProject);
        acProject.setName(AC_PROJECT_NAME);
        dsiProject = new DSIProject();
        dsiProject.addRepositories(dsiRepository);
        dsiProject.setAcProject(acProject);

    }

    private void initializeGHLabelAssumingRepositoryInitialized() {
        //let's create a GHLabel associated with the existing project
        ghLabel = new GHLabel();
        ghLabel.setName(GHLabel.PROJECT_PREFIX + AC_PROJECT_NAME);
        ghLabel.setRepository(ghRepository);
        ghLabel.setUrl(HTTP_LABEL_PHONY_URL);
    }

    private void initializeGHMilestoneAssumingRepositoryInitialized() {
        ghMilestone = new GHMilestone();
        ghMilestone.setRepository(ghRepository);

    }

    //TODO depended on must have the ACTaskCategory
    //TODO depended on must have the GHLabel

    @Before
    @Atomic
    public void init() throws IOException {
        TestUtils.clearInstancesWithRoot();
        acTask = new ACTask();

        acTask.setName(AC_TASK_NAME);
        acTask.setBody(AC_TASK_BODY);
        acTask.setComplete(Boolean.FALSE);
        dsiIssue = new DSIIssue();
        acTask.setDsiObjectIssue(dsiIssue);

        initializeTaskCategoryRepositoryAndACProject();
        acTask.setProject(acProject);
        acTask.setTaskCategory(acTaskCategory);

        MaidRoot.setGitHubClient(gitHubClient);

        when(taskMock.getId()).thenReturn(12l);
        when(gitHubClient.post(Mockito.anyString(), Mockito.any(Object.class), Mockito.any(Type.class))).thenReturn(taskMock);

        //let's create the repository to be associated with the milestones
//        ACProject acProject = new ACProject();
//        acProject.addMilestones(ghMilestoneTwo);
//        acProject.addMilestones(ghMilestoneOne);
//        acProject.setId(AC_PROJECT_ID);

//        ghMilestone.setTitle(GH_MILESTONE_TITLE);
//        ghMilestone.setDescription(GH_MILESTONE_DESCRIPTION);
//        ghMilestone.setDueOn(GH_MILESTONE_DUE_ON_LT);

    }

    @Mock
    Issue taskMock;

    @Mock
    GitHubClient gitHubClient;

    @Mock
    GitHubResponse response;

    @Captor
    ArgumentCaptor<Issue> issueCaptor;

    @Captor
    ArgumentCaptor<Label> labelCaptor;

    @Captor
    ArgumentCaptor<GitHubRequest> ghRequestCaptor;

    @Captor
    ArgumentCaptor<String> stringPostUriCaptor;

    @SuppressWarnings("static-access")
    @Test
    @Atomic(mode = TxMode.WRITE)
    public void createWithoutMilestoneAndReusingExistingLabel() throws IOException {
        //an issue on the GH side should be created. Let's make sure it mirrors the
        //issue passed as an argument, as expected
        initializeGHLabelAssumingRepositoryInitialized();
//        when(gitHubClient.get(Mockito.any(GitHubRequest.class))).then(new Answer<GitHubResponse>( {
//
//            @Override
//            public GitHubResponse answer(InvocationOnMock invocation) throws Throwable {
//                GitHubRequest request = invocation.getArguments()[0];
//                if (request.getType().equals(Repository.class)) {
//                    //let's return a repository with the
//                }
//                return response;
//            }
//
//
//        }));
//
        when(gitHubClient.get(Mockito.any(GitHubRequest.class))).thenReturn(response);
        SyncEvent createWithoutMilestoneEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, acTask,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(pt.ist.maidSyncher.api.activeCollab.ACTask.class)));

        SyncActionWrapper sync = acTask.sync(createWithoutMilestoneEvent);

        sync.sync();

        verify(gitHubClient).post(Mockito.anyString(), issueCaptor.capture(), Mockito.any(Type.class));

        Label labelExpected = new Label();
        labelExpected.setName(GHLabel.PROJECT_PREFIX + AC_PROJECT_NAME);
        assertEquals(AC_TASK_BODY, issueCaptor.getValue().getBodyHtml());
        assertEquals(AC_TASK_NAME, issueCaptor.getValue().getTitle());
        assertTrue(issueCaptor.getValue().getLabels().contains(labelExpected));

    }

    /**
     * Tests the creation of the GHIssue, and also the creation of the P-"name_of_project" GHLabel.
     * 
     * There will be no milestone associated with the AC task, thus, no milestone will be used/created on the GH side
     * 
     */
    @SuppressWarnings("static-access")
    @Atomic(mode = TxMode.WRITE)
    @Test
    public void createWithoutMilestoneAndWithoutLabel() throws IOException {

        final Label labelMock = mock(Label.class);
        when(labelMock.getUrl()).thenReturn("http://smthng.com/smthng");
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

        SyncEvent createWithoutMilestoneEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, acTask,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(pt.ist.maidSyncher.api.activeCollab.ACTask.class)));

        SyncActionWrapper sync = acTask.sync(createWithoutMilestoneEvent);

        sync.sync();

        verify(gitHubClient).post(Mockito.anyString(), issueCaptor.capture(), Mockito.eq(Issue.class));
        verify(gitHubClient).post(Mockito.anyString(), labelCaptor.capture(), Mockito.eq(Label.class));

        assertEquals(AC_TASK_BODY, issueCaptor.getValue().getBodyHtml());
        assertEquals(AC_TASK_NAME, issueCaptor.getValue().getTitle());

        assertEquals(GHLabel.PROJECT_PREFIX + AC_PROJECT_NAME, labelCaptor.getValue().getName());

    }

    @SuppressWarnings("static-access")
    @Atomic(mode = TxMode.WRITE)
    @Test
    public void createReusingExistingMilestoneAndExistingLabel() throws IOException {
        //we must create a label on the other side
        initializeGHLabelAssumingRepositoryInitialized();

        initializeGHMilestoneAssumingRepositoryInitialized();

        when(gitHubClient.get(Mockito.any(GitHubRequest.class))).thenAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                GitHubRequest gitHubRequest = (GitHubRequest) invocation.getArguments()[0];
                GitHubResponse gitHubResponseMock = mock(GitHubResponse.class);
                if (gitHubRequest.getType().equals(Milestone.class)) {
                    Milestone mockMilestone = mock(Milestone.class);
                    doReturn(mockMilestone).when(gitHubResponseMock).getBody();
                }
                if (gitHubRequest.getType().equals(Label.class)) {
                    Label mockLabel = mock(Label.class);
                    doReturn(mockLabel).when(gitHubResponseMock).getBody();
                }

                return gitHubResponseMock;
            }
        });

        SyncEvent createWithoutMilestoneEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, acTask,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(pt.ist.maidSyncher.api.activeCollab.ACTask.class)));

        SyncActionWrapper sync = acTask.sync(createWithoutMilestoneEvent);

        sync.sync();

        verify(gitHubClient).post(Mockito.anyString(), issueCaptor.capture(), Mockito.eq(Issue.class));

        assertEquals(AC_TASK_BODY, issueCaptor.getValue().getBodyHtml());
        assertEquals(AC_TASK_NAME, issueCaptor.getValue().getTitle());

        assertEquals(GHLabel.PROJECT_PREFIX + AC_PROJECT_NAME, labelCaptor.getValue().getName());

        //we should have had 2 'GET's, one to the label and the other to the milestone
        verify(gitHubClient, times(2)).get(ghRequestCaptor.capture());

        GitHubRequest milestoneRequest = null;
        GitHubRequest labelRequest = null;
        for (GitHubRequest gitHubRequest : ghRequestCaptor.getAllValues()) {
            if (gitHubRequest.getType().equals(Milestone.class))
                milestoneRequest = gitHubRequest;
            else if (gitHubRequest.getType().equals(Label.class))
                labelRequest = gitHubRequest;
        }

        assertTrue("We should have done one milestone request", milestoneRequest != null);
        assertTrue("We should have done a label request", labelRequest != null);

    }

    public void updateSimpleFields() {

    }

    public void updateWithTaskCategoryChange() {

    }

    public void updateWithMilestoneChange() {

    }

}
