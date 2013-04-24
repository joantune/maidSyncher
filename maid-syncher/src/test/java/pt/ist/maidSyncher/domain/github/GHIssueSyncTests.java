/**
 * 
 */
package pt.ist.maidSyncher.domain.github;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jvstm.TransactionalCommand;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.service.IssueService;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import pt.ist.fenixframework.pstm.Transaction;
import pt.ist.maidSyncher.api.activeCollab.ACMilestone;
import pt.ist.maidSyncher.api.activeCollab.ACObject;
import pt.ist.maidSyncher.api.activeCollab.ACSubTask;
import pt.ist.maidSyncher.api.activeCollab.ACTask;
import pt.ist.maidSyncher.api.activeCollab.interfaces.RequestProcessor;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SyncEvent;
import pt.ist.maidSyncher.domain.SyncEvent.SyncUniverse;
import pt.ist.maidSyncher.domain.SyncEvent.TypeOfChangeEvent;
import pt.ist.maidSyncher.domain.SynchableObject;
import pt.ist.maidSyncher.domain.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.activeCollab.ACTaskCategory;
import pt.ist.maidSyncher.domain.dsi.DSIIssue;
import pt.ist.maidSyncher.domain.dsi.DSIMilestone;
import pt.ist.maidSyncher.domain.dsi.DSIRepository;
import pt.ist.maidSyncher.domain.dsi.DSISubTask;
import pt.ist.maidSyncher.domain.exceptions.SyncEventOriginObjectChanged;
import pt.ist.maidSyncher.domain.sync.APIObjectWrapper;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 2 de Abr de 2013
 * 
 *         Tests the behaviour of synching a GHIssue
 */
public class GHIssueSyncTests extends FFTest {

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

    @Before
    public void initMockGHIssue() {
        Transaction.withTransaction(new TransactionalCommand() {

            @Override
            public void doIt() {
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

                mockGHRepository.setDsiObjectRepository(mockDSIRepository);
                ghIssueToUse.setDsiObjectIssue(mockDSIIssue);
                mockDSIRepository.setDefaultProject(mockACProject);

                ACTaskCategory acTaskCategory = new ACTaskCategory();
                acTaskCategory.setId(123);

                mockACProject.addTaskCategoriesDefined(acTaskCategory);
                mockDSIRepository.addAcTaskCategories(acTaskCategory);

                createSyncEvent = syncEventGenerator(TypeOfChangeEvent.CREATE, ghIssueToUse);
                updateSyncEvent = syncEventGenerator(TypeOfChangeEvent.UPDATE, ghIssueToUse);

            }
        });
    }

    private SyncEvent syncEventGenerator(final TypeOfChangeEvent typeOfChangeEvent, final SynchableObject originObject) {
        return syncEventGenerator(typeOfChangeEvent, originObject, Collections.<PropertyDescriptor> emptySet());
    }

    private SyncEvent syncEventGenerator(final TypeOfChangeEvent typeOfChangeEvent, final SynchableObject originObject,
            Collection<PropertyDescriptor> changedDescriptors) {
        return new SyncEvent(new LocalTime(), typeOfChangeEvent, changedDescriptors, null, new APIObjectWrapper() {

            @Override
            public void validateAPIObject() throws SyncEventOriginObjectChanged {
                return;
            }

            @Override
            public Object getAPIObject() {
                return null;
            }
        }, SyncUniverse.getTargetSyncUniverse(originObject), originObject);
    }

    private final static String GHISSUE_TITLE = "Test issue";
    private final static String GHISSUE_DESCRIPTION = "Test on the description, this must be the same";
    @Test
    public void createOpenGHIssueWithoutMilestone() throws Exception {

        when(requestProcessor.processPost(Mockito.any(ACObject.class), Mockito.anyString())).then(new Answer<JSONObject>() {

            @Override
            public JSONObject answer(InvocationOnMock invocation) throws Throwable {
                ACTask acTask = (ACTask) invocation.getArguments()[0];
                //Verify that the name is the same
                assertEquals(acTask.getName(), GHISSUE_TITLE);
                assertEquals(acTask.getBody(), GHISSUE_DESCRIPTION);
                return new JSONObject();
            }
        });
        ACObject.setRequestProcessor(requestProcessor);

        Transaction.withTransaction(new TransactionalCommand() {

            @Override
            public void doIt() {
                //let us say that the issue is open
                ghIssueToUse.setState(GHIssue.STATE_OPEN);


                //let us use a custom name
                try {
                    final SyncActionWrapper sync = ghIssueToUse.sync(createSyncEvent);
                    sync.sync();
                } catch (IOException e) {
                    throw new Error(e);
                }

            }
        });

        //making sure we tried to create the ACTask at least once
        verify(requestProcessor, times(1)).processPost(Mockito.any(ACTask.class), Mockito.anyString());
    }

    @Test
    public void createOpenGHIssueWithMilestone() throws Exception {


        final String ghMilestoneTitle = "testMilestone1";
        final Date ghMilestoneDueOn = new LocalDate().toDateMidnight().toDate();
        when(requestProcessor.processPost(Mockito.any(ACObject.class), Mockito.anyString())).then(new Answer<JSONObject>() {

            @Override
            public JSONObject answer(InvocationOnMock invocation) throws Throwable {
                ACObject argumentOne = (ACObject) invocation.getArguments()[0];
                if (argumentOne instanceof ACTask) {
                    ACTask acTask = (ACTask) argumentOne;
                    //Verify that the name is the same
                    assertEquals(acTask.getName(), GHISSUE_TITLE);
                    assertEquals(acTask.getBody(), GHISSUE_DESCRIPTION);
                } else if (argumentOne instanceof ACMilestone) {
                    ACMilestone acMilestone = (ACMilestone) argumentOne;
                    assertEquals(acMilestone.getName(), ghMilestoneTitle);
                    assertEquals(acMilestone.getDueOn(), ghMilestoneDueOn);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", 123l);
                    jsonObject.put("project_id", mockACProject.getId());
                    return jsonObject;
                }
                return new JSONObject();
            }
        });
        ACObject.setRequestProcessor(requestProcessor);

        //let's init the milestone
        initMilestone(ghMilestoneTitle, ghMilestoneDueOn);

        Transaction.withTransaction(new TransactionalCommand() {

            @Override
            public void doIt() {
                //let us say that the issue is open
                ghIssueToUse.setState(GHIssue.STATE_OPEN);

                //let us use a custom name
                try {
                    final SyncActionWrapper sync = ghIssueToUse.sync(createSyncEvent);
                    sync.sync();
                } catch (IOException e) {
                    throw new Error(e);
                }

            }
        });

        //making sure we tried to create the ACTask at least once
        verify(requestProcessor, times(2)).processPost(Mockito.any(ACTask.class), Mockito.anyString());
    }

    private void initMilestone(final String milestoneTitle, final Date dueDate) {
        Transaction.withTransaction(new TransactionalCommand() {

            @Override
            public void doIt() {
                GHMilestone ghMilestone = new GHMilestone();
                ghMilestone.setTitle(milestoneTitle);
                ghMilestone.setDueOn(new LocalTime(dueDate));
                ghMilestone.setRepository(mockGHRepository);
                DSIMilestone dsiMilestone = new DSIMilestone();
                ghMilestone.setDsiObjectMilestone(dsiMilestone);
                ghIssueToUse.setMilestone(ghMilestone);

            }
        });

    }

    @Test
    public void checkThatNoSyncTakesPlaceIfDeleted() throws IOException {
        Transaction.withTransaction(new TransactionalCommand() {

            @Override
            public void doIt() {
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
        });

    }

    private static pt.ist.maidSyncher.domain.activeCollab.ACTask acTask;
    private static pt.ist.maidSyncher.domain.activeCollab.ACProject acProject;

    final private void initACTaskAssociatedWithGHIssue() {
        acTask = new pt.ist.maidSyncher.domain.activeCollab.ACTask();
        acTask.setDsiObjectIssue(mockDSIIssue);
        acProject = new ACProject();
        acProject.setId(12);
        acTask.setProject(acProject);
    }

    DSISubTask dsiSubTask;

    private static final String GHISSUE_SUBTASK_BODY = "Body without the prefix";
    private static final String GHISSUE_SUBTASK_TITLE = "subtask title :)";

    private static GHIssue parentGHIssue;

    TransactionalCommand initIssueAsSubTask = new TransactionalCommand() {

        @Override
        public void doIt() {
            parentGHIssue = new GHIssue();
            parentGHIssue.setRepository(mockGHRepository);
            parentGHIssue.setNumber(3);

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

            ghIssueToUse.removeDsiObjectIssue();
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


        }
    };

    @Test
    public void updateAsSubTask() throws IOException {
        //setup the issue to be a subtask
        Transaction.withTransaction(initIssueAsSubTask);

        //the changed descriptors will be all of the descriptors
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(new Issue());

        final SyncEvent updateSubTaskSyncEvent =
                syncEventGenerator(TypeOfChangeEvent.UPDATE, ghIssueToUse, Arrays.asList(propertyDescriptors));

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

        Transaction.withTransaction(new TransactionalCommand() {

            @Override
            public void doIt() {

                MaidRoot.getInstance().setGitHubClient(mockGHClient);
                final SyncActionWrapper sync = ghIssueToUse.sync(updateSubTaskSyncEvent);

                //verify the issueservice calls and the number of calls to the requestProcessor

                try {
                    sync.sync();
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
        });

    }

    public void updateSimpleWithoutLabelOrMilestoneChange() throws IOException {

        //the changed descriptors will be all of the descriptors ...
        // ... minus the label and milestone
        Collection<PropertyDescriptor> propertyDescriptorsToUse = Collections2.filter(Arrays.asList(PropertyUtils.getPropertyDescriptors(new Issue())), new Predicate<PropertyDescriptor>() {
            @Override
            public boolean apply(PropertyDescriptor input)
            {
                if (input == null)
                    return false;
                if (input.getName().equals(GHIssue.DSC_MILESTONE) || input.getName().equals(GHIssue.DSC_LABELS)) {
                    return false;
                }
                return true;
            }
        });
        final SyncEvent updateTaskSyncEvent =
                syncEventGenerator(TypeOfChangeEvent.UPDATE, ghIssueToUse, propertyDescriptorsToUse);

        //setting up the mocks
        JSONObject mockACTaskJsonObjectPostResult = mock(JSONObject.class);
        when(requestProcessor.processPost(Mockito.any(ACTask.class), Mockito.anyString())).thenReturn(
                mockACTaskJsonObjectPostResult);
        ACObject.setRequestProcessor(requestProcessor);


        //yep, this is the way I should verify things, the code in the above tests is ugly
        //and are usages of mockito as it shouldn't be used. this is the way to go:) Issue #15
        ArgumentCaptor<ACTask> acTaskGenerated = ArgumentCaptor.forClass(ACTask.class);
        verify(requestProcessor).processPost(acTaskGenerated.capture(), Mockito.anyString());
//        asser



    }
}
