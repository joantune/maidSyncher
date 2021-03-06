/**
 * 
 */
package pt.ist.maidSyncher.domain.offlineTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.service.IssueService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
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
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.activeCollab.ACMilestone;
import pt.ist.maidSyncher.domain.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.activeCollab.ACSubTask;
import pt.ist.maidSyncher.domain.activeCollab.ACTask;
import pt.ist.maidSyncher.domain.activeCollab.ACTaskCategory;
import pt.ist.maidSyncher.domain.dsi.DSIIssue;
import pt.ist.maidSyncher.domain.dsi.DSIMilestone;
import pt.ist.maidSyncher.domain.dsi.DSIProject;
import pt.ist.maidSyncher.domain.dsi.DSIRepository;
import pt.ist.maidSyncher.domain.dsi.DSISubTask;
import pt.ist.maidSyncher.domain.github.GHIssue;
import pt.ist.maidSyncher.domain.github.GHLabel;
import pt.ist.maidSyncher.domain.github.GHMilestone;
import pt.ist.maidSyncher.domain.github.GHRepository;
import pt.ist.maidSyncher.domain.github.GHUser;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncEvent;
import pt.ist.maidSyncher.domain.sync.SyncEvent.TypeOfChangeEvent;
import pt.ist.maidSyncher.domain.test.utils.OfflineSyncTests;
import pt.ist.maidSyncher.domain.test.utils.TestUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 29 de Abr de 2013
 * 
 *         Tests the synching {@link ACTask#sync(SyncEvent)} method/functionality
 */
@Category(OfflineSyncTests.class)
@RunWith(MockitoJUnitRunner.class)
public class ACTaskSyncTest {

    static final int GH_SUBTASK_ONE_ISSUE_NUMBER = 23;

    private static final String GH_LABEL_SIMPLE_LABEL_NAME = "GH LABEL SIMPLE LABEL NAME";

    static final String AC_SUB_TASK_ONE_NAME = "AC SUB TASK ONE NAME";

    private static final String OTHER_GH_REPOSITORY_NAME = "OTHER GH REPOSITORY NAME";

    private static final int GH_ISSUE_NUMBER = new Random().nextInt(3000);

    private static final String AC_MILESTONE_BODY = "ac milestone body";

    private static final String HTTP_LABEL_PHONY_URL = "http://label/phony/url";

    private static final String GH_REP_OWNER_LOGIN = "owner";

    ACTask acTask;

    ACMilestone acMilestone;

    private final static String AC_TASK_NAME = "ac task name";
    private final static String AC_TASK_BODY = "ac task body";

    private static final String GH_REPOSITORY_NAME = "testRepo";

    DSIMilestone dsiMilestone;

    ACTaskCategory acTaskCategory;

    GHRepository ghRepository;

    private final static String AC_TASK_REPOSITORY_NAME = ACTaskCategory.REPOSITORY_PREFIX + GH_REPOSITORY_NAME;

    private final static long GH_REPOSITORY_ID = 3;

    private final static String GH_REPOSITORY_URL = "http://smthing/repos/" + GH_REPOSITORY_ID;

    protected static final String AC_PROJECT_NAME = "projectName";

    static final int GH_MILESTONE_NUMBER = new Random().nextInt(2000);

    protected static final String GH_MILESTONE_TITLE = "gh milestone title";

    private static final DateTime AC_MILESTONE_DUEON = new DateTime();

    DSIRepository dsiRepository;

    ACProject acProject;

    GHLabel ghLabel;

    DSIProject dsiProject;

    DSIIssue dsiIssue;

    GHMilestone ghMilestone;

    GHUser repOwner;

    void initializeTaskCategoryRepositoryAndACProject() {

        acTaskCategory = new ACTaskCategory();
        acTaskCategory.setName(AC_TASK_REPOSITORY_NAME);
        dsiRepository = new DSIRepository();
        dsiRepository.addAcTaskCategories(acTaskCategory);

        repOwner = new GHUser();
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

    void initializeGHLabelAssumingRepositoryInitialized() {
        //let's create a GHLabel associated with the existing project
        ghLabel = new GHLabel();
        ghLabel.setName(GHLabel.PROJECT_PREFIX + AC_PROJECT_NAME);
        ghLabel.setRepository(ghRepository);
        ghLabel.setUrl(HTTP_LABEL_PHONY_URL);
    }

    void initializeGHMilestoneAssumingRepositoryInitialized() {
        ghMilestone = new GHMilestone();
        ghMilestone.setRepository(ghRepository);
        ghMilestone.setNumber(GH_MILESTONE_NUMBER);
        ghMilestone.setTitle(GH_MILESTONE_TITLE);

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
        acTask.setVisibility(true);

        MaidRoot.setGitHubClient(gitHubClient);

        when(taskMock.getId()).thenReturn(12l);
        when(gitHubClient.post(Mockito.anyString(), Mockito.any(Object.class), Mockito.eq(Issue.class))).thenReturn(taskMock);

        when(labelMock.getUrl()).thenReturn("http://phony.url/");

    }

    @Mock
    Issue taskMock;

    @Mock
    GitHubClient gitHubClient;

    @Mock
    GitHubResponse response;

//    @Captor
//    ArgumentCaptor<Issue> issueCaptor;

    @Captor
    ArgumentCaptor<GitHubRequest> ghRequestCaptor;

    @Captor
    ArgumentCaptor<String> stringPostUriCaptor;

    @Captor
    ArgumentCaptor<Map<Object, Object>> postCaptor;

    @Captor
    ArgumentCaptor<Label> labelCaptor;

    @Captor
    ArgumentCaptor<Milestone> milestoneCaptor;

    /**
     * This test is to show that if no ACTaskCategory is set, nothing should be done
     * 
     * @throws IOException
     */
    @Atomic(mode = TxMode.WRITE)
    @Test
    public void createWithoutTaskCategory() throws IOException {
        acTask.setTaskCategory(null);
        SyncEvent createWithoutTaskCategoryEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, acTask,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(pt.ist.maidSyncher.api.activeCollab.ACTask.class)));

        SyncActionWrapper sync = acTask.sync(createWithoutTaskCategoryEvent);

        sync.sync();

        verify(gitHubClient, never()).post(Mockito.anyString(), Mockito.anyObject(), Mockito.any(Type.class));

    }

    @SuppressWarnings("static-access")
    @Test
    @Atomic(mode = TxMode.WRITE)
    public void createWithoutMilestoneAndReusingExistingLabel() throws IOException {
        //an issue on the GH side should be created. Let's make sure it mirrors the
        //issue passed as an argument, as expected
        initializeGHLabelAssumingRepositoryInitialized();

        when(gitHubClient.get(Mockito.any(GitHubRequest.class))).thenReturn(response);
        SyncEvent createWithoutMilestoneEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, acTask,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(pt.ist.maidSyncher.api.activeCollab.ACTask.class)));

        SyncActionWrapper sync = acTask.sync(createWithoutMilestoneEvent);

        sync.sync();

        Map<Object, Object> issueMap = validateIssueSimpleFieldAndReturnIssueMap();
        List<String> labelNames = (List<String>) issueMap.get(IssueService.FILTER_LABELS);
        assertTrue(labelNames.contains(GHLabel.PROJECT_PREFIX + AC_PROJECT_NAME));

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
    public void createWithoutMilestoneAndWithoutExistingLabel() throws IOException {

        setupStubToReplyWithPhonyLabel();

        SyncEvent createWithoutMilestoneEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, acTask,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(pt.ist.maidSyncher.api.activeCollab.ACTask.class)));

        SyncActionWrapper sync = acTask.sync(createWithoutMilestoneEvent);

        sync.sync();

        Map<Object, Object> issueMap = validateIssueSimpleFieldAndReturnIssueMap();

        verify(gitHubClient).post(Mockito.anyString(), labelCaptor.capture(), Mockito.eq(Label.class));

        assertEquals(GHLabel.PROJECT_PREFIX + AC_PROJECT_NAME, labelCaptor.getValue().getName());

    }

    private void setupStubToReplyWithPhonyLabel() throws IOException {
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
    }

    @SuppressWarnings("static-access")
    @Atomic(mode = TxMode.WRITE)
    @Test
    public void createReusingExistingMilestoneAndExistingLabel() throws IOException {
        //we must create a label on the other side
        initializeGHLabelAssumingRepositoryInitialized();

        initializeGHMilestoneAssumingRepositoryInitialized();

        //we must set a milestone with the same name of the GH one
        initializeACMilestone();

        //let's intercept the creation of a milestone and retrieve a number to verify
        //later that that number is used on the creation of the issue

        SyncEvent createWithoutMilestoneEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, acTask,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(pt.ist.maidSyncher.api.activeCollab.ACTask.class)));

        SyncActionWrapper sync = acTask.sync(createWithoutMilestoneEvent);

        sync.sync();

        Map<Object, Object> issueMap = validateIssueSimpleFieldAndReturnIssueMap();

        assertTrue(((List<String>) issueMap.get(IssueService.FILTER_LABELS)).contains(GHLabel.PROJECT_PREFIX + AC_PROJECT_NAME));

        //assert the milestone is there with the correct number
        assertEquals(String.valueOf(GH_MILESTONE_NUMBER), issueMap.get(IssueService.FILTER_MILESTONE));

    }

    void initializeACMilestone() {
        acMilestone = new ACMilestone();
        acMilestone.setDueOn(AC_MILESTONE_DUEON);
        acMilestone.setBody(AC_MILESTONE_BODY);
        acMilestone.setName(GH_MILESTONE_TITLE);
        acMilestone.setProject(acProject);
        acTask.setMilestone(acMilestone);
        if (dsiMilestone == null) {
            dsiMilestone = new DSIMilestone();
        }
        acMilestone.setDsiObjectMilestone(dsiMilestone);
    }

    @Mock
    Milestone milestoneMock;

    private GHIssue ghIssue;

    @Test
    @Atomic(mode = TxMode.WRITE)
    public void createCreatingNonExistingMilestoneAndExistingLabel() throws IOException {
        //we must create a label on the other side
        initializeGHLabelAssumingRepositoryInitialized();

        //we will have a milestone on the AC side, but none on the GH side
        //so we should have an attempt of creation of the ghmilestone
        initializeACMilestone();

        //let's intercept the milestone creation and return a mock milestone
        //with a valid url
        when(milestoneMock.getUrl()).thenReturn("http://some.valid.url");
        when(milestoneMock.getNumber()).thenReturn(GH_MILESTONE_NUMBER);

        when(gitHubClient.post(Mockito.anyString(), Mockito.anyObject(), Mockito.eq(Milestone.class))).thenReturn(milestoneMock);

        SyncEvent createWithMilestoneEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, acTask,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(pt.ist.maidSyncher.api.activeCollab.ACTask.class)));

        SyncActionWrapper sync = acTask.sync(createWithMilestoneEvent);

        sync.sync();

        //making sure we called the post twice
        verify(gitHubClient, times(2)).post(Mockito.anyString(), Mockito.any(Object.class), Mockito.any(Type.class));

        //let's intercept the milestone creation
        verify(gitHubClient).post(Mockito.anyString(), milestoneCaptor.capture(), Mockito.eq(Milestone.class));

        assertEquals(GH_MILESTONE_TITLE, milestoneCaptor.getValue().getTitle());
        assertEquals(AC_MILESTONE_BODY, milestoneCaptor.getValue().getDescription());
        assertEquals(AC_MILESTONE_DUEON.toDate(), milestoneCaptor.getValue().getDueOn());

        Map<Object, Object> issueMap = validateIssueSimpleFieldAndReturnIssueMap();

        assertTrue(((List<String>) issueMap.get(IssueService.FILTER_LABELS)).contains(GHLabel.PROJECT_PREFIX + AC_PROJECT_NAME));

        //assert the milestone is there with the correct number
        assertEquals(String.valueOf(GH_MILESTONE_NUMBER), issueMap.get(IssueService.FILTER_MILESTONE));

    }

    void initializeAssociatedGHIssue() {
        ghIssue = new GHIssue();
        ghIssue.setRepository(ghRepository);
        ghIssue.setDsiObjectIssue(dsiIssue);
        ghIssue.setNumber(GH_ISSUE_NUMBER);

    }

    @Mock
    Label labelMock;

    @Atomic(mode = TxMode.WRITE)
    @Test
    public void updateSimpleFields() throws IOException {

        //let's create the corresponding GHIssue
        initializeAssociatedGHIssue();

        //let's get the property descriptors for the right fields
        Collection<PropertyDescriptor> propertyDescriptorsToUse =
                Collections2.filter(
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(pt.ist.maidSyncher.api.activeCollab.ACTask.class)),
                        new Predicate<PropertyDescriptor>() {
                            @Override
                            public boolean apply(PropertyDescriptor input) {
                                if (input == null)
                                    return false;
                                if (input.getName().equals(ACTask.DSC_MILESTONE_ID)
                                        || input.getName().equals(ACTask.DSC_CATEGORY_ID)
                                        || input.getName().equals(ACTask.DSC_PROJECT_ID) || input.getName().equals(ACTask.DSC_ID)) {
                                    return false;
                                }
                                return true;
                            }
                        });

        SyncEvent updateSimpleFieldsEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.UPDATE, acTask, propertyDescriptorsToUse);
        SyncActionWrapper sync = acTask.sync(updateSimpleFieldsEvent);
        sync.sync();

        Map<Object, Object> issueMap = validateIssueSimpleFieldAndReturnIssueMap();

    }

    @Atomic(mode = TxMode.WRITE)
    @Test
    public void updateWithProjectChange() throws IOException {
        initializeAssociatedGHIssue();

        //let's have a previous GHLabel associated with this
        //issue
        initializeGHLabelAssumingRepositoryInitialized();
        ghLabel.setName(GH_LABEL_SIMPLE_LABEL_NAME);
        ghIssue.addLabels(ghLabel);

        //setup the stubs
        when(gitHubClient.post(Mockito.anyString(), Mockito.anyObject(), Mockito.eq(Label.class))).then(new Answer<Label>() {

            @Override
            public Label answer(InvocationOnMock invocation) throws Throwable {
                Label label = (Label) invocation.getArguments()[1];
                if (label.getUrl() == null)
                    label.setUrl("http://phonyUrl.com/");
                return label;
            }

        });

        //let's get the property descriptors for the right fields
        Collection<PropertyDescriptor> propertyDescriptorsToUse =
                Collections2.filter(
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(pt.ist.maidSyncher.api.activeCollab.ACTask.class)),
                        new Predicate<PropertyDescriptor>() {
                            @Override
                            public boolean apply(PropertyDescriptor input) {
                                if (input == null)
                                    return false;
                                if (input.getName().equals(ACTask.DSC_MILESTONE_ID)
                                        || input.getName().equals(ACTask.DSC_CATEGORY_ID)
                                        || input.getName().equals(ACTask.DSC_ID)) {
                                    return false;
                                }
                                return true;
                            }
                        });

        SyncEvent updateWithProjectChangeEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.UPDATE, acTask, propertyDescriptorsToUse);

        SyncActionWrapper sync = acTask.sync(updateWithProjectChangeEvent);

        sync.sync();

        //verifications
        verify(gitHubClient).post(Mockito.anyString(), labelCaptor.capture(), Mockito.eq(Label.class));

        assertEquals(GHLabel.PROJECT_PREFIX + AC_PROJECT_NAME, labelCaptor.getValue().getName());

        Map<Object, Object> issueMap = validateIssueSimpleFieldAndReturnIssueMap();

        List<String> labels = (List<String>) issueMap.get(IssueService.FILTER_LABELS);

        assertTrue(labels.contains(GHLabel.PROJECT_PREFIX + AC_PROJECT_NAME));
        assertTrue(labels.contains(GH_LABEL_SIMPLE_LABEL_NAME));

    }

    private Map<Object, Object> validateIssueSimpleFieldAndReturnIssueMap() throws IOException {
        verify(gitHubClient).post(Mockito.anyString(), postCaptor.capture(), Mockito.eq(Issue.class));

        Map<Object, Object> issueMap = postCaptor.getValue();

        assertEquals(AC_TASK_BODY, issueMap.get(IssueService.FIELD_BODY));
        assertEquals(AC_TASK_NAME, issueMap.get(IssueService.FIELD_TITLE));
        return issueMap;
    }

    public final static String OTHER_AC_TASK_CATEGORY_NAME = ACTaskCategory.REPOSITORY_PREFIX + OTHER_GH_REPOSITORY_NAME;

    private static final String AC_SUB_TASK_TWO_NAME = "AC sub task two name";

    @Atomic(mode = TxMode.WRITE)
    @Test
    public void updateWithTaskCategoryChange() throws IOException {
        initializeAssociatedGHIssue();
        ghIssue.setTitle(AC_TASK_NAME);
        ghIssue.setBody(AC_TASK_BODY);

        //let's initialize the second repository
        //and the TaskCategory
        GHRepository otherGhRepository = new GHRepository();
        otherGhRepository.setId(230);
        otherGhRepository.setOwner(repOwner);
        otherGhRepository.setName(OTHER_GH_REPOSITORY_NAME);

        DSIRepository otherDsiRepository = new DSIRepository();

        ACTaskCategory otherAcTaskCategory = new ACTaskCategory();
        otherAcTaskCategory.setName(OTHER_AC_TASK_CATEGORY_NAME);
        otherAcTaskCategory.setDsiObjectRepository(otherDsiRepository);
        otherDsiRepository.setGitHubRepository(otherGhRepository);
        acTask.setTaskCategory(otherAcTaskCategory);

        //let's initialize the Milestone, so that we can check if the corresponding
        //milestone on the other repository is created
        initializeACMilestone();

        initializeSubTaskAndCorrespondingGHIssue(acTask, AC_SUB_TASK_ONE_NAME, ghRepository, true);

        initializeSubTaskAndCorrespondingGHIssue(acTask, AC_SUB_TASK_TWO_NAME, ghRepository, true);

        //setup the stubs
        setupStubToReplyWithPhonyMilestone(gitHubClient);

        //let's get the property descriptors for the right fields
        Collection<PropertyDescriptor> propertyDescriptorsToUse =
                TestUtils.propertyDescriptorsToUseMinus(pt.ist.maidSyncher.api.activeCollab.ACTask.class, ACTask.DSC_PROJECT_ID,
                        ACTask.DSC_MILESTONE_ID, ACTask.DSC_ID);

        SyncEvent updateWithTaskCategoryChange =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.UPDATE, acTask, propertyDescriptorsToUse);

        SyncActionWrapper sync = acTask.sync(updateWithTaskCategoryChange);

        sync.sync();

        //verifications

        //the simple fields of the new issue

        //one to close the issue, other
        //to open the other one, and two similar ones for
        //each of the two subtasks, plus one for the other values of the issue that got edited
        verify(gitHubClient, times(6)).post(Mockito.anyString(), postCaptor.capture(), Mockito.eq(Issue.class));

//        Map<Object, Object> issueMap = postCaptor.getValue();
        boolean detectedTheClosedMainIssue = false;
        boolean detectedTheClosedSubTaskOneIssue = false;
        boolean detectedTheClosedSubTaskTwoIssue = false;

        boolean detectedTheOpenedMainIssue = false;
        boolean detectedTheOpenedSubTaskOne = false;
        boolean detectedTheOpenedSubTaskTwo = false;
        for (Map<Object, Object> issueMap : postCaptor.getAllValues()) {
            //we should have two issueMaps, one for the old issue, and another
            //for the new one
            String state = (String) issueMap.get(IssueService.FILTER_STATE);
            if (IssueService.STATE_CLOSED.equals(state)) {
                String issueTitle = (String) issueMap.get(IssueService.FIELD_TITLE);
                switch (issueTitle) {
                case AC_TASK_NAME:
                    //then this is the old one
                    detectedTheClosedMainIssue = true;
                    break;
                case AC_SUB_TASK_ONE_NAME:
                    detectedTheClosedSubTaskOneIssue = true;
                    break;
                case AC_SUB_TASK_TWO_NAME:
                    detectedTheClosedSubTaskTwoIssue = true;
                    break;
                default:
                    fail("unexpected issue caught");
                    break;
                }

                //this one should have the deleted label
                List<String> labelsString = (List<String>) issueMap.get(IssueService.FILTER_LABELS);
                assertTrue(labelsString.contains(GHLabel.DELETED_LABEL_NAME));
                //the description must have the - moved to XX -
                String body = (String) issueMap.get(IssueService.FIELD_BODY);
                //TODO ? make sure the creation of the issue gives out a number, and assert that
                //the number is here ?
                body.contains(GHIssue.MOVED_TO_PREFIX + otherGhRepository.generateId());

            } else {
                String issueTitle = (String) issueMap.get(IssueService.FIELD_TITLE);
                switch (issueTitle) {
                case AC_TASK_NAME:
                    detectedTheOpenedMainIssue = true;
                    assertEquals(AC_TASK_BODY, issueMap.get(IssueService.FIELD_BODY));
                    break;
                case AC_SUB_TASK_ONE_NAME:
                    detectedTheOpenedSubTaskOne = true;
                    break;
                case AC_SUB_TASK_TWO_NAME:
                    detectedTheOpenedSubTaskTwo = true;
                    break;

                default:
                    fail("unexpected open issue caught");
                    break;

                }

            }

        }

        assertTrue(detectedTheClosedMainIssue);
        assertTrue(detectedTheClosedSubTaskOneIssue);
        assertTrue(detectedTheClosedSubTaskTwoIssue);

        assertTrue(detectedTheOpenedMainIssue);
        assertTrue(detectedTheOpenedSubTaskOne);
        assertTrue(detectedTheOpenedSubTaskTwo);

        //a creation of a GHMilestone on the other side
        verify(gitHubClient).post(Mockito.anyString(), milestoneCaptor.capture(), Mockito.eq(Milestone.class));

        Milestone capturedMilestone = milestoneCaptor.getValue();
        assertEquals(GH_MILESTONE_TITLE, capturedMilestone.getTitle());

    }

    private void setupStubToReplyWithPhonyMilestone(final GitHubClient mockedGitHubClient) throws IOException {
        when(mockedGitHubClient.post(Mockito.anyString(), Mockito.anyObject(), Mockito.eq(Milestone.class))).then(
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

    }

    @Atomic(mode = TxMode.WRITE)
    @Test
    public void updateFromNonGHSideToGHSide() throws IOException {

        //let's also init the milestone to make sure it is created
        initializeACMilestone();
        //and subtasks (without GH side)
        initializeSubTaskAndCorrespondingGHIssue(acTask, AC_SUB_TASK_ONE_NAME, null, false);
        initializeSubTaskAndCorrespondingGHIssue(acTask, AC_SUB_TASK_TWO_NAME, null, false);

        //setup the stubs
        setupStubToReplyWithPhonyMilestone(gitHubClient);

        setupStubToReplyWithPhonyLabel();

        //let's get the property descriptors for the right fields
        Collection<PropertyDescriptor> propertyDescriptorsToUse =
                TestUtils.propertyDescriptorsToUseMinus(pt.ist.maidSyncher.api.activeCollab.ACTask.class, ACTask.DSC_PROJECT_ID,
                        ACTask.DSC_MILESTONE_ID, ACTask.DSC_ID);

        SyncEvent updateToCreatGHSide = TestUtils.syncEventGenerator(TypeOfChangeEvent.UPDATE, acTask, propertyDescriptorsToUse);

        SyncActionWrapper sync = acTask.sync(updateToCreatGHSide);

        sync.sync();

        //verifications

        //the simple fields of the new issue

        //one to open the issue, and two similar ones for
        //each of the two subtasks
        verify(gitHubClient, times(3)).post(Mockito.anyString(), postCaptor.capture(), Mockito.eq(Issue.class));

//        Map<Object, Object> issueMap = postCaptor.getValue();

        boolean detectedTheOpenedMainIssue = false;
        boolean detectedTheOpenedSubTaskOne = false;
        boolean detectedTheOpenedSubTaskTwo = false;
        for (Map<Object, Object> issueMap : postCaptor.getAllValues()) {
            //we should have two issueMaps, one for the old issue, and another
            //for the new one
            String state = (String) issueMap.get(IssueService.FILTER_STATE);
            if (IssueService.STATE_CLOSED.equals(state)) {
                String issueTitle = (String) issueMap.get(IssueService.FIELD_TITLE);
                switch (issueTitle) {
                default:
                    fail("unexpected issue closing caught");
                    break;
                }

            } else {
                String issueTitle = (String) issueMap.get(IssueService.FIELD_TITLE);
                switch (issueTitle) {
                case AC_TASK_NAME:
                    detectedTheOpenedMainIssue = true;
                    assertEquals(AC_TASK_BODY, issueMap.get(IssueService.FIELD_BODY));
                    break;
                case AC_SUB_TASK_ONE_NAME:
                    detectedTheOpenedSubTaskOne = true;
                    break;
                case AC_SUB_TASK_TWO_NAME:
                    detectedTheOpenedSubTaskTwo = true;
                    break;

                default:
                    fail("unexpected open issue caught");
                    break;

                }

            }

        }

        assertTrue(detectedTheOpenedMainIssue);
        assertTrue(detectedTheOpenedSubTaskOne);
        assertTrue(detectedTheOpenedSubTaskTwo);

        //a creation of a GHMilestone on the other side
        verify(gitHubClient).post(Mockito.anyString(), milestoneCaptor.capture(), Mockito.eq(Milestone.class));

        Milestone capturedMilestone = milestoneCaptor.getValue();
        assertEquals(GH_MILESTONE_TITLE, capturedMilestone.getTitle());

    }

    @Atomic(mode = TxMode.WRITE)
    @Test
    public void updateFromGHSideToNonGHSide() throws IOException {

        //let's also init the milestone to make sure it is created
        initializeACMilestone();
        //and subtasks (without GH side)
        initializeSubTaskAndCorrespondingGHIssue(acTask, AC_SUB_TASK_ONE_NAME, ghRepository, true);
        initializeSubTaskAndCorrespondingGHIssue(acTask, AC_SUB_TASK_TWO_NAME, ghRepository, true);

        //let's remove the task category - taking out the 'ghSide'
        acTask.setTaskCategory(null);

        //setup the stubs
        setupStubToReplyWithPhonyMilestone(gitHubClient);

        setupStubToReplyWithPhonyLabel();

        //let's get the property descriptors for the right fields
        Collection<PropertyDescriptor> propertyDescriptorsToUse =
                TestUtils.propertyDescriptorsToUseMinus(pt.ist.maidSyncher.api.activeCollab.ACTask.class, ACTask.DSC_PROJECT_ID,
                        ACTask.DSC_MILESTONE_ID, ACTask.DSC_ID);

        SyncEvent updateToCreatGHSide = TestUtils.syncEventGenerator(TypeOfChangeEvent.UPDATE, acTask, propertyDescriptorsToUse);

        SyncActionWrapper sync = acTask.sync(updateToCreatGHSide);

        sync.sync();

        //verifications

        //nothing should have been done
        verify(gitHubClient, never()).post(Mockito.anyString(), postCaptor.capture(), Mockito.eq(Issue.class));

        //a creation of a GHMilestone on the other side
        verify(gitHubClient, never()).post(Mockito.anyString(), milestoneCaptor.capture(), Mockito.eq(Milestone.class));

    }

    static ACSubTask initializeSubTaskAndCorrespondingGHIssue(ACTask parentTask, String subTaskName,
            GHRepository ghRepositoryToUse, boolean createCorrespondingGHSide) {
        //let's create a couple of subtasks to make sure that they are also moved
        ACSubTask acSubTaskOne = new ACSubTask();
        DSISubTask subTaskOne = new DSISubTask(parentTask.getDsiObjectIssue());

        acSubTaskOne.setName(subTaskName);
        acSubTaskOne.setTask(parentTask);
        acSubTaskOne.setDsiObjectSubTask(subTaskOne);
        if (createCorrespondingGHSide) {
            GHIssue ghIssueAssociatedWithSubTaskOne = new GHIssue();

            ghIssueAssociatedWithSubTaskOne.setRepository(ghRepositoryToUse);
            ghIssueAssociatedWithSubTaskOne.setTitle(subTaskName);
            ghIssueAssociatedWithSubTaskOne.setDsiObjectSubTask(subTaskOne);
            ghIssueAssociatedWithSubTaskOne.setNumber(GH_SUBTASK_ONE_ISSUE_NUMBER);
        } else {
            if (acSubTaskOne.getDsiObjectSubTask().getGhIssue() != null)
                acSubTaskOne.getDsiObjectSubTask().setGhIssue(null);

        }
        return acSubTaskOne;
    }

    @SuppressWarnings("static-access")
    @Atomic(mode = TxMode.WRITE)
    @Test
    public void updateWithMilestoneChange() throws IOException {
        initializeAssociatedGHIssue();

        initializeACMilestone();

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
        //let's get the property descriptors for the right fields
        Collection<PropertyDescriptor> propertyDescriptorsToUse =
                Collections2.filter(
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(pt.ist.maidSyncher.api.activeCollab.ACTask.class)),
                        new Predicate<PropertyDescriptor>() {
                            @Override
                            public boolean apply(PropertyDescriptor input) {
                                if (input == null)
                                    return false;
                                if (input.getName().equals(ACTask.DSC_PROJECT_ID)
                                        || input.getName().equals(ACTask.DSC_CATEGORY_ID)
                                        || input.getName().equals(ACTask.DSC_ID)) {
                                    return false;
                                }
                                return true;
                            }
                        });

        SyncEvent updateWithMilestoneChangeEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.UPDATE, acTask, propertyDescriptorsToUse);

        SyncActionWrapper sync = acTask.sync(updateWithMilestoneChangeEvent);

        sync.sync();

        //verifications
        verify(gitHubClient).post(Mockito.anyString(), milestoneCaptor.capture(), Mockito.eq(Milestone.class));

        assertEquals(GH_MILESTONE_TITLE, milestoneCaptor.getValue().getTitle());
        assertEquals(AC_MILESTONE_BODY, milestoneCaptor.getValue().getDescription());

        Map<Object, Object> issueMap = validateIssueSimpleFieldAndReturnIssueMap();

        int ghMilestoneNumber = Integer.valueOf((String) issueMap.get(IssueService.FILTER_MILESTONE));

        assertEquals(GH_MILESTONE_NUMBER, ghMilestoneNumber);

    }

}
