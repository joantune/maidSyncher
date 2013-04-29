/**
 * 
 */
package pt.ist.maidSyncher.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.service.IssueService;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.json.simple.JSONObject;
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
import pt.ist.maidSyncher.api.activeCollab.ACMilestone;
import pt.ist.maidSyncher.api.activeCollab.ACObject;
import pt.ist.maidSyncher.api.activeCollab.ACSubTask;
import pt.ist.maidSyncher.api.activeCollab.ACTask;
import pt.ist.maidSyncher.api.activeCollab.interfaces.RequestProcessor;
import pt.ist.maidSyncher.domain.SyncEvent.TypeOfChangeEvent;
import pt.ist.maidSyncher.domain.activeCollab.ACProject;
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
import pt.ist.maidSyncher.domain.test.utils.TestUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 2 de Abr de 2013
 * 
 *         Tests the behaviour of synching a GHIssue
 */
@RunWith(MockitoJUnitRunner.class)
public class GHIssueSyncTest {

//    private static Set<PropertyDescriptor> possiblePropertyDescriptor = new HashSet();
//
//    static {
//        try {
//            possiblePropertyDescriptor.add(new PropertyDescriptor(GHIssue.DSC_ID, GHIssue.class));
//            possiblePropertyDescriptor.add(new PropertyDescriptor(GHIssue.DSC_URL, GHIssue.class));
//            possiblePropertyDescriptor.add(new PropertyDescriptor(GHIssue.DSC_HTML_URL, GHIssue.class));
//            possiblePropertyDescriptor.add(new PropertyDescriptor(GHIssue.DSC_CREATED_AT, GHIssue.class));
//            possiblePropertyDescriptor.add(new PropertyDescriptor(GHIssue.DSC_UPDATED_AT, GHIssue.class));
//            possiblePropertyDescriptor.add(new PropertyDescriptor(GHIssue.DSC_NUMBER, GHIssue.class));
//            possiblePropertyDescriptor.add(new PropertyDescriptor(GHIssue.DSC_MILESTONE, GHIssue.class));
//            possiblePropertyDescriptor.add(new PropertyDescriptor(GHIssue.DSC_LABELS, GHIssue.class));
//            possiblePropertyDescriptor.add(new PropertyDescriptor(GHIssue.DSC_STATE, GHIssue.class));
//            possiblePropertyDescriptor.add(new PropertyDescriptor(GHIssue.DSC_BODY, GHIssue.class));
//            possiblePropertyDescriptor.add(new PropertyDescriptor(GHIssue.DSC_TITLE, GHIssue.class));
//        } catch (IntrospectionException e) {
//            throw new Error(e);
//        }
//    }

    private static GHIssue ghIssueToUse;
    private static GHRepository mockGHRepository;
    private static DSIRepository mockDSIRepository;

    private static DSIIssue mockDSIIssue;

    private static ACProject mockACProject;
    private static RequestProcessor requestProcessor;

    private static SyncEvent createSyncEvent;
    private static SyncEvent updateSyncEvent;

    private static boolean initialized = false;

    private static void clearRoot() {
        //let's clear out the objects
        MaidRoot instance = MaidRoot.getInstance();

        instance.getAcObjectsSet().clear();
        instance.getDsiObjectsSet().clear();
        instance.getGhCommentsSet().clear();
        instance.getGhIssuesSet().clear();
        instance.getGhLabelsSet().clear();
        instance.getGhMilestonesSet().clear();
//        instance.getGhOrganization().
        instance.getGhRepositoriesSet().clear();
        instance.getGhUsersSet().clear();

    }

    @Before
    @Atomic
    public void initMockGHIssue() {

        clearRoot();
        requestProcessor = mock(RequestProcessor.class);
        //let's create a usable GHRepository
        mockGHRepository = new GHRepository();
        mockGHRepository.setName("test_repository");
        GHUser ghUser = new GHUser();
        mockGHRepository.setOwner(ghUser);
        ghUser.setOrganization(MaidRoot.getInstance().getGhOrganization());
        ghUser.setName("test_user");
        ghUser.setLogin("test_user");
        mockGHRepository.setName("test_repository");

        mockDSIRepository = new DSIRepository();
        mockDSIIssue = new DSIIssue();
        mockACProject = new ACProject();
        mockACProject.setId(123);

        ghIssueToUse = new GHIssue();
        ghIssueToUse.setRepository(mockGHRepository);
        //when(ghIssueToUse.getRepository()).thenReturn(mockGHRepository);
        ghIssueToUse.setTitle(GHISSUE_TITLE);
        ghIssueToUse.setBodyHtml(GHISSUE_DESCRIPTION);
        ghIssueToUse.setState(GHIssue.STATE_CLOSED);

        mockGHRepository.setDsiObjectRepository(mockDSIRepository);
        ghIssueToUse.setDsiObjectIssue(mockDSIIssue);
        mockDSIRepository.setDefaultProject(mockACProject);

        ACTaskCategory acTaskCategory = new ACTaskCategory();
        acTaskCategory.setId(123);

        mockACProject.addTaskCategoriesDefined(acTaskCategory);
        mockDSIRepository.addAcTaskCategories(acTaskCategory);

        createSyncEvent = TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, ghIssueToUse);
        updateSyncEvent = TestUtils.syncEventGenerator(TypeOfChangeEvent.UPDATE, ghIssueToUse);

    }



    private final static String GHISSUE_TITLE = "Test issue";
    private final static String GHISSUE_DESCRIPTION = "Test on the description, this must be the same";

    @Mock
    private JSONObject mockJSONObject;

    @Captor
    private ArgumentCaptor<ACTask> apiAcTaskCaptor;

    @Captor
    private ArgumentCaptor<ACObject> apiAcObjectCaptor;

    @Test
    @Atomic
    public void createOpenGHIssueWithoutMilestone() throws Exception {

        when(requestProcessor.processPost(Mockito.any(ACObject.class), Mockito.anyString())).thenReturn(mockJSONObject);

        ACObject.setRequestProcessor(requestProcessor);

        //let us say that the issue is open
        ghIssueToUse.setState(GHIssue.STATE_OPEN);

        //let us use a custom name
        try {
            final SyncActionWrapper sync = ghIssueToUse.sync(createSyncEvent);
            sync.sync();
        } catch (IOException e) {
            throw new Error(e);
        }

        //making sure we tried to create the ACTask at least once
        verify(requestProcessor, times(1)).processPost(apiAcTaskCaptor.capture(), Mockito.anyString());

        //let's check on the values
        assertEquals(apiAcTaskCaptor.getValue().getName(), GHISSUE_TITLE);
        assertEquals(apiAcTaskCaptor.getValue().getBody(), GHISSUE_DESCRIPTION);
    }

    @Captor
    private ArgumentCaptor<ACMilestone> apiAcMilestoneCaptor;

    @Atomic(mode = TxMode.WRITE)
    @Test
    public void createOpenGHIssueWithMilestone() throws Exception {

        final Date ghMilestoneDueOn = new LocalDate().toDateMidnight().toDate();

        when(mockJSONObject.get("id")).thenReturn(123l);
        when(mockJSONObject.get("project_id")).thenReturn(mockACProject.getId());
        when(requestProcessor.processPost(Mockito.any(ACObject.class), Mockito.anyString())).thenReturn(mockJSONObject);
//        when(requestProcessor.processPost(Mockito.any(ACMilestone.class), Mockito.anyString())).then(new Answer<JSONObject>() {
//            when(requestProcessor.processPost(Mockito.any(ACMilestone.class), Mockito.anyString())).then(new Answer<JSONObject>() {
//
//                @Override
//                public JSONObject answer(InvocationOnMock invocation) throws Throwable {
//                    ACObject argumentOne = (ACObject) invocation.getArguments()[0];
//                    if (argumentOne instanceof ACMilestone) {
//                        JSONObject jsonObject = new JSONObject();
//                        jsonObject.put("id", 123l);
//                        jsonObject.put("project_id", mockACProject.getId());
//                        return jsonObject;
//                    }
//                    return new JSONObject();
//                }
//            });
        ACObject.setRequestProcessor(requestProcessor);

        //let's init the milestone
        initMilestone(GHMILESTONE_TITLE, ghMilestoneDueOn, GHMILESTONE_DESCRIPTION);

        //let us say that the issue is open
        ghIssueToUse.setState(GHIssue.STATE_OPEN);

        //let us use a custom name
        try {
            final SyncActionWrapper sync = ghIssueToUse.sync(createSyncEvent);
            sync.sync();
        } catch (IOException e) {
            throw new Error(e);
        }

//      we should have two invocations of the processPost
        verify(requestProcessor, times(2)).processPost(apiAcObjectCaptor.capture(), Mockito.anyString());

        //let's confirm the values
        for (ACObject acObject : apiAcObjectCaptor.getAllValues()) {
            if (acObject instanceof ACTask) {
                ACTask acTask = (ACTask) acObject;
                assertEquals(acTask.getName(), GHISSUE_TITLE);
                assertEquals(acTask.getBody(), GHISSUE_DESCRIPTION);
            } else if (acObject instanceof ACMilestone) {
                ACMilestone acMilestone = (ACMilestone) acObject;
                assertEquals(acMilestone.getName(), GHMILESTONE_TITLE);
                assertEquals(acMilestone.getDueOn(), ghMilestoneDueOn);
                assertEquals(acMilestone.getBody(), GHMILESTONE_DESCRIPTION);

            }
        }

    }

    private GHMilestone initMilestone(final String milestoneTitle, final Date dueDate, String ghMilestoneBody) {

        GHMilestone ghMilestone = new GHMilestone();
        ghMilestone.setTitle(milestoneTitle);
        ghMilestone.setDueOn(new LocalTime(dueDate));
        ghMilestone.setDescription(ghMilestoneBody);
        ghMilestone.setRepository(mockGHRepository);
        DSIMilestone dsiMilestone = new DSIMilestone();
        ghMilestone.setDsiObjectMilestone(dsiMilestone);
        ghIssueToUse.setMilestone(ghMilestone);
        return ghMilestone;

    }

    @Test
    @Atomic
    public void checkThatNoSyncTakesPlaceIfDeleted() throws IOException {
        //let's put it with the deleted label
        GHLabel deletedGhLabel = new GHLabel();
        deletedGhLabel.setRepository(mockGHRepository);
        deletedGhLabel.setName(GHLabel.DELETED_LABEL_NAME);
        ghIssueToUse.addLabels(deletedGhLabel);

        try {
            ghIssueToUse.sync(createSyncEvent).sync();
            //making sure we never tried to create an ACTask
            verify(requestProcessor, never()).processPost(Mockito.any(ACTask.class), Mockito.anyString());

            //in the update we must already have an ACTask associated
            initACTaskAssociatedWithGHIssue();

            ghIssueToUse.sync(updateSyncEvent).sync();
            //making sure we never tried to create an ACTask
            verify(requestProcessor, never()).processPost(Mockito.any(ACTask.class), Mockito.anyString());

        } catch (IOException e) {
            throw new Error(e);
        }

    }

    private static pt.ist.maidSyncher.domain.activeCollab.ACTask acTask;

    final private void initACTaskAssociatedWithGHIssue() {
        acTask = new pt.ist.maidSyncher.domain.activeCollab.ACTask();
        acTask.setDsiObjectIssue(mockDSIIssue);
        acTask.setUrl("http://phonyUrl");
        acTask.setProject(mockACProject);
    }

    DSISubTask dsiSubTask;

    private static final String GHISSUE_SUBTASK_BODY = "Body without the prefix";
    private static final String GHISSUE_SUBTASK_TITLE = "subtask title :)";

    private static GHIssue parentGHIssue;

    @Test
    @Atomic
    public void updateAsSubTask() throws IOException {
        //setup the issue to be a subtask
        parentGHIssue = new GHIssue();
        parentGHIssue.setRepository(mockGHRepository);
        parentGHIssue.setNumber(3);

        initACTaskAssociatedWithGHIssue();

        dsiSubTask = new DSISubTask(mockDSIIssue);
        mockDSIIssue.setGhIssue(parentGHIssue);
        pt.ist.maidSyncher.domain.activeCollab.ACSubTask acSubTask = new pt.ist.maidSyncher.domain.activeCollab.ACSubTask();
        acSubTask.setComplete(false);
        acSubTask.setName("blahBlah");
        acSubTask.setId(3);
        acSubTask.setTask(acTask);
        acSubTask.setUrl("xpto");

        acTask.setId(3);
        dsiSubTask.setAcSubTask(acSubTask);

        ghIssueToUse.setDsiObjectIssue(null);
        ghIssueToUse.setDsiObjectSubTask(dsiSubTask);
        ghIssueToUse.setState(GHIssue.STATE_CLOSED);
        ghIssueToUse.setBody(GHISSUE_SUBTASK_BODY);
        ghIssueToUse.setTitle(GHISSUE_SUBTASK_TITLE);
        ghIssueToUse.setId(123);

        //just for fun, let's put some labels here
        GHLabel ghLabel = new GHLabel();
        ghLabel.setName("blahblah");
        ghLabel.setRepository(mockGHRepository);
        ghIssueToUse.addLabels(ghLabel);

        //the changed descriptors will be all of the descriptors
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(new Issue());

        final SyncEvent updateSubTaskSyncEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.UPDATE, ghIssueToUse, Arrays.asList(propertyDescriptors));

        //let's set up the mocks

        when(requestProcessor.processPost(Mockito.any(ACObject.class), Mockito.anyString())).then(new Answer<JSONObject>() {

            @Override
            public JSONObject answer(InvocationOnMock invocation) throws Throwable {
                ACSubTask acSubTask = (ACSubTask) invocation.getArguments()[0];
                //Verify that the name is the same
                assertEquals(acSubTask.getName(), GHISSUE_SUBTASK_TITLE);
                assertTrue(acSubTask.isComplete());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("parent_class", ACTask.CLASS_VALUE);
                jsonObject.put("parent_id", 3);
                return jsonObject;
            }
        });
        ACObject.setRequestProcessor(requestProcessor);

        final GitHubClient mockGHClient = mock(GitHubClient.class);
        GitHubResponse mockGHresponse = mock(GitHubResponse.class);
//        doReturn(mockGHresponse).when(mockGHClient).get(Mockito.any(GitHubRequest.class));
        when(mockGHClient.post(Mockito.anyString(), Mockito.any(Object.class), Mockito.any(Type.class))).then(
                new Answer<Issue>() {

                    @Override
                    public Issue answer(InvocationOnMock invocation) throws Throwable {
                        Issue issue = mock(Issue.class);
                        doReturn(123l).when(issue).getId();
                        return issue;
                    }
                });

        MaidRoot.getInstance().setGitHubClient(mockGHClient);
        final SyncActionWrapper sync = ghIssueToUse.sync(updateSubTaskSyncEvent);

        try {
            sync.sync();
            //verify the issueservice calls and the number of calls to the requestProcessor
            verify(requestProcessor, times(1)).processPost(Mockito.any(ACSubTask.class), Mockito.anyString());
            Map<String, String> params = new HashMap();
            params.put(IssueService.FIELD_BODY, ghIssueToUse.applySubTaskBodyPrefix(GHISSUE_SUBTASK_BODY));
            params.put(IssueService.FIELD_TITLE, GHISSUE_SUBTASK_TITLE);
            params.put(IssueService.FILTER_STATE, GHIssue.STATE_CLOSED);

            verify(mockGHClient).post("/repos/test_user/test_repository/issues/0", params, Issue.class);
            verify(mockGHClient, times(1)).post(Mockito.anyString(), Mockito.any(Object.class), Mockito.any(Type.class));
        } catch (IOException e) {
            throw new Error(e);
        }

    }

    @Test
    @Atomic
    public void updateSimpleWithoutLabelOrMilestoneChange() throws IOException {

        //the changed descriptors will be all of the descriptors ...
        // ... minus the label and milestone
        Collection<PropertyDescriptor> propertyDescriptorsToUse =
                Collections2.filter(Arrays.asList(PropertyUtils.getPropertyDescriptors(new Issue())),
                        new Predicate<PropertyDescriptor>() {
                    @Override
                    public boolean apply(PropertyDescriptor input) {
                        if (input == null)
                            return false;
                        if (input.getName().equals(GHIssue.DSC_MILESTONE) || input.getName().equals(GHIssue.DSC_LABELS)) {
                            return false;
                        }
                        return true;
                    }
                });
        final SyncEvent updateTaskSyncEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.UPDATE, ghIssueToUse, propertyDescriptorsToUse);

        //setting up the mocks
        when(requestProcessor.processPost(Mockito.any(ACTask.class), Mockito.anyString())).thenReturn(mockJSONObject);
        ACObject.setRequestProcessor(requestProcessor);

        //executing the 'deed' and initializing the ACTask
        //we need to have an ACTask on the other side
        initACTaskAssociatedWithGHIssue();

        SyncActionWrapper sync = ghIssueToUse.sync(updateTaskSyncEvent);

        try {
            sync.sync();
        } catch (IOException e) {
            throw new Error(e);
        }

        //yep, this is the way I should verify things, the code in the above tests is ugly
        //and are usages of mockito as it shouldn't be used. this is the way to go:) Issue #15
        verify(requestProcessor).processPost(apiAcTaskCaptor.capture(), Mockito.anyString());

        assertEquals(GHISSUE_TITLE, apiAcTaskCaptor.getValue().getName());
        assertEquals(GHISSUE_DESCRIPTION, apiAcTaskCaptor.getValue().getBody());
        assertEquals(Boolean.TRUE, apiAcTaskCaptor.getValue().getComplete());

    }

    private static final String GHMILESTONE_TITLE = "Milestone title";
    private static final String GHMILESTONE_DESCRIPTION = "Milestone description";

    @Test
    @Atomic(mode = TxMode.WRITE)
    public void updateTaskWithExistingMilestoneAndNoLabelChange() throws IOException {
        //the changed descriptors will be all of the descriptors ...
        // ... minus the label
        Collection<PropertyDescriptor> propertyDescriptorsToUse =
                Collections2.filter(Arrays.asList(PropertyUtils.getPropertyDescriptors(new Issue())),
                        new Predicate<PropertyDescriptor>() {
                    @Override
                    public boolean apply(PropertyDescriptor input) {
                        if (input == null)
                            return false;
                        if (input.getName().equals(GHIssue.DSC_LABELS)) {
                            return false;
                        }
                        return true;
                    }
                });
        final SyncEvent updateTaskSyncEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.UPDATE, ghIssueToUse, propertyDescriptorsToUse);

        //setting up the mocks
        JSONObject mockACTaskJsonObjectPostResult = mock(JSONObject.class);
        when(requestProcessor.processPost(Mockito.any(ACTask.class), Mockito.anyString())).thenReturn(
                mockACTaskJsonObjectPostResult);
        ACObject.setRequestProcessor(requestProcessor);

        //executing the 'deed' and initializing the ACTask
        //we need to have an ACTask on the other side
        initACTaskAssociatedWithGHIssue();
        //we should take care of the milestone as well
        Date milestoneDueDate = new Date();
        GHMilestone ghMilestone = initMilestone(GHMILESTONE_TITLE, milestoneDueDate, GHMILESTONE_DESCRIPTION);

        //we should have the milestone on the AC side
        pt.ist.maidSyncher.domain.activeCollab.ACMilestone mockAcMilestone =
                new pt.ist.maidSyncher.domain.activeCollab.ACMilestone();
//                mock(pt.ist.maidSyncher.domain.activeCollab.ACMilestone.class); TODO we can't have mocks with objects that aren't mocks
        mockAcMilestone.setName(GHMILESTONE_TITLE);
        mockAcMilestone.setId(321l);
        mockACProject.addMilestones(mockAcMilestone);

        SyncActionWrapper sync = ghIssueToUse.sync(updateTaskSyncEvent);

        try {
            sync.sync();
        } catch (IOException e) {
            throw new Error(e);
        }

        verify(requestProcessor).processPost(apiAcTaskCaptor.capture(), Mockito.anyString());

        assertEquals(321l, apiAcTaskCaptor.getValue().getMilestoneId());

    }

    @Test
    @Atomic
    public void updateTaskWithLabelAndNonExistingMilestoneChange() throws IOException {
        //the changed descriptors will be all of the descriptors
        final SyncEvent updateTaskSyncEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.UPDATE, ghIssueToUse,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(new Issue())));

        when(requestProcessor.getBasicUrlForPath(Mockito.anyString())).then(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                String argumentOne = (String) invocation.getArguments()[0];
                return "https://baseString/" + argumentOne;
            }
        });

        //setting up the mocks
        final JSONObject mockACTaskJsonObjectPostResult = mock(JSONObject.class);
        when(requestProcessor.processPost(Mockito.any(ACObject.class), Mockito.anyString())).then(new Answer<JSONObject>() {
            //let's craft the JSONObject to return

            @Override
            public JSONObject answer(InvocationOnMock invocation) throws Throwable {
                Object argumentOne = invocation.getArguments()[0];
                if (argumentOne != null && argumentOne instanceof ACMilestone) {
                    ACMilestone acMilestone = (ACMilestone) invocation.getArguments()[0];
                    JSONObject jsonObjectToReturn = new JSONObject();
                    if (acMilestone != null) {
                        //let's get the project id on the url
                        String url = (String) invocation.getArguments()[1];
                        String projectId = StringUtils.substringBetween(url, "projects/", "/milestones");
                        jsonObjectToReturn.put("project_id", Integer.valueOf(projectId));
                        jsonObjectToReturn.put("id", new Random().nextInt(100) + 1000);
                        jsonObjectToReturn.put("name", acMilestone.getName());
                        jsonObjectToReturn.put("body", acMilestone.getBody());
                    }
                    return jsonObjectToReturn;
                } else
                    return mockACTaskJsonObjectPostResult;
            }
        });

        when(requestProcessor.processPost(Mockito.anyString(), Mockito.anyString())).thenReturn(mockACTaskJsonObjectPostResult);

        ACObject.setRequestProcessor(requestProcessor);

        //executing the 'deed' and initializing the ACTask
        //we need to have an ACTask on the other side
        initACTaskAssociatedWithGHIssue();
        //we should take care of the milestone as well
        Date milestoneDueDate = new Date();
        GHMilestone ghMilestone = initMilestone(GHMILESTONE_TITLE, milestoneDueDate, GHMILESTONE_DESCRIPTION);

        //we should have a label as well, with a different project associated
        initLabelWithAssociatedProject();
        SyncActionWrapper sync = ghIssueToUse.sync(updateTaskSyncEvent);

        try {
            sync.sync();
        } catch (IOException e) {
            throw new Error(e);
        }

        //we should have:

        //the creation of the ACMilestone on the 'otherAcProject';
        verify(requestProcessor,times(2)).processPost(apiAcObjectCaptor.capture(), Mockito.anyString());

        for (ACObject acObject  : apiAcObjectCaptor.getAllValues()) {
            if (acObject instanceof ACMilestone) {
                ACMilestone acMilestone = (ACMilestone) acObject;
                assertEquals(AC_OTHER_PROJECT_ID.longValue(), acMilestone.getProjectId());
            }
            else if (acObject instanceof ACTask) {
                ACTask acTask = (ACTask) acObject;
                assertEquals(GHISSUE_TITLE, acTask.getName());
                assertEquals( GHISSUE_DESCRIPTION, acTask.getBody());
                assertEquals( Boolean.TRUE, acTask.getComplete());
            }
            else {
                fail();
            }
        }



        //the 'move' of the ACTask to the 'otherAcProject';
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> moveContentCaptor = ArgumentCaptor.forClass(String.class);

        verify(requestProcessor).processPost(pathCaptor.capture(), moveContentCaptor.capture());

        String pathValue = pathCaptor.getValue();
        assertTrue(pathValue != null && pathValue.contains("tasks"));
        assertTrue(pathValue != null && pathValue.contains("move-to-project"));
        assertTrue(pathValue != null && pathValue.contains("123")); //the old project id

        String contentValue = moveContentCaptor.getValue();
        assertTrue(contentValue != null && contentValue.contains("move_to_project_id=" + AC_OTHER_PROJECT_ID.longValue()));
//        assertTrue(contentValue != null && contentValue.contains("submitted=submitted")); -- this part is not done by the stubbed request processor

        //the updates to the simple fields of the actask


    }

    private static final String AC_OTHER_PROJECT_NAME = "Project XPTO";
    private static final Long AC_OTHER_PROJECT_ID = new Long(33);
    private static final String GH_PROJECT_LABEL = GHLabel.PROJECT_PREFIX + AC_OTHER_PROJECT_NAME;

    private void initLabelWithAssociatedProject() {
        GHLabel ghLabel = new GHLabel();
        ghLabel.setName(GH_PROJECT_LABEL);
        ghLabel.setRepository(mockGHRepository);

        DSIProject dsiProject = new DSIProject();
        ACProject otherAcProject = new ACProject();
        otherAcProject.setId(AC_OTHER_PROJECT_ID);
        otherAcProject.setName(AC_OTHER_PROJECT_NAME);
        ghLabel.setDsiObjectProject(dsiProject);
        dsiProject.setAcProject(otherAcProject);
        ghIssueToUse.addLabels(ghLabel);
    }
}
