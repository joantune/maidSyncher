/**
 * 
 */
package pt.ist.maidSyncher.changesBuzz.sync;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.Collections;

import jvstm.TransactionalCommand;

import org.joda.time.LocalTime;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import pt.ist.fenixframework.pstm.Transaction;
import pt.ist.maidSyncher.api.activeCollab.ACObject;
import pt.ist.maidSyncher.api.activeCollab.ACTask;
import pt.ist.maidSyncher.api.activeCollab.interfaces.RequestProcessor;
import pt.ist.maidSyncher.changesBuzz.FFTest;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SyncEvent;
import pt.ist.maidSyncher.domain.SyncEvent.SyncUniverse;
import pt.ist.maidSyncher.domain.SyncEvent.TypeOfChangeEvent;
import pt.ist.maidSyncher.domain.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.activeCollab.ACTaskCategory;
import pt.ist.maidSyncher.domain.dsi.DSIIssue;
import pt.ist.maidSyncher.domain.dsi.DSIRepository;
import pt.ist.maidSyncher.domain.exceptions.SyncEventOriginObjectChanged;
import pt.ist.maidSyncher.domain.github.GHIssue;
import pt.ist.maidSyncher.domain.github.GHRepository;
import pt.ist.maidSyncher.domain.github.GHUser;
import pt.ist.maidSyncher.domain.sync.APIObjectWrapper;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;

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

    public void initMockGHIssue() {
        Transaction.withTransaction(new TransactionalCommand() {

            @Override
            public void doIt() {
                if (ghIssueToUse == null) {
                    //let's create a usable GHRepository
                    mockGHRepository = new GHRepository();
                    GHUser ghUser = new GHUser();
                    mockGHRepository.setOwner(ghUser);
                    ghUser.setOrganization(MaidRoot.getInstance().getGhOrganization());

                    mockDSIRepository = new DSIRepository();
                    mockDSIIssue = new DSIIssue();
                    mockACProject = new ACProject();

                    ghIssueToUse = new GHIssue();
                    ghIssueToUse.setRepository(mockGHRepository);
                    //when(ghIssueToUse.getRepository()).thenReturn(mockGHRepository);

                    mockGHRepository.setDsiObjectRepository(mockDSIRepository);
                    ghIssueToUse.setDsiObjectIssue(mockDSIIssue);
                    mockDSIRepository.setDefaultProject(mockACProject);

                    ACTaskCategory acTaskCategory = new ACTaskCategory();
                    acTaskCategory.setId(123);

                    mockACProject.addTaskCategoriesDefined(acTaskCategory);
                    mockDSIRepository.addAcTaskCategories(acTaskCategory);

                }

            }
        });
    }

//    @PrepareForTest({ ACTask.class })
    @Test
    public void createOpenGHIssueWithoutMilestone() throws Exception {
        initMockGHIssue();

//        ACObject.setRequestProcessor(new RequestProcessor() {
//
//            @Override
//            public JSONObject processPost(String path, String string) throws IOException {
//                return mock(JSONObject.class);
//            }
//
//            @Override
//            public JSONObject processPost(ACObject acObject, String relativePath) throws IOException {
//                return mock(JSONObject.class);
//            }
//
//            @Override
//            public Object processGet(String _url) throws IOException {
//                return mock(Object.class);
//            }
//
//            @Override
//            public String getBasicUrlForPath(String string) {
//                return null;
//            }
//        });

//        whenNew(ACTask.class).withAnyArguments().thenReturn(new ACTask());

        RequestProcessor requestProcessor = mock(RequestProcessor.class);
        when(requestProcessor.processPost(Mockito.any(ACObject.class), Mockito.anyString())).thenReturn(new JSONObject());
        ACObject.setRequestProcessor(requestProcessor);
//        mockStatic(ACTask.class);
//        mockStatic(JsonRest.class);
//        when(JsonRest.getInt(new JSONObject(), null)).thenReturn(0);
//        when(JsonRest.getBooleanFromInt(Mockito.any(JSONObject.class), Mockito.anyString())).thenReturn(Boolean.FALSE);

//        when(ACTask.createTask(Mockito.any(ACTask.class), Mockito.anyLong())).then(new Answer<ACTask>() {

//        @Override
//        public ACTask answer(InvocationOnMock invocation) throws Throwable {
//            ACTask acTaskToReturn = (ACTask) invocation.getArguments()[0];
//            acTaskToReturn.setId(123);
//            return acTaskToReturn;
//        }
//    });

//        when(mockRequestProcessor.processPost(Mockito.any(ACObject.class), Mockito.anyString()), new Answer<JSONObject>() {
//
//            @Override
//            public JSONObject answer(InvocationOnMock invocation) throws Throwable {
//                ACObject preliminarObject = (ACObject) invocation.getArguments()[0];
//                preliminarObject.setId(new Random().nextInt());
//                JSONObject jsonObjectToReturn = new JSONObject();
//                jsonObjectToReturn.
//
//                return null;
//            }
//        };

//        ACObject.setRequestProcessor(mockRequestProcessor);

//        Assert.assertEquals(mockGHRepository, ghIssueToUse.getRepository());

        final SyncEvent syncEvent =
                new SyncEvent(new LocalTime(), TypeOfChangeEvent.CREATE, Collections.<PropertyDescriptor> emptySet(), null,
                        new APIObjectWrapper() {

                    @Override
                    public void validateAPIObject() throws SyncEventOriginObjectChanged {
                        return;
                    }

                    @Override
                    public Object getAPIObject() {
                        return null;
                    }
                }, SyncUniverse.ACTIVE_COLLAB, ghIssueToUse);

        Transaction.withTransaction(new TransactionalCommand() {

            @Override
            public void doIt() {
                //let us say that the issue is open
                ghIssueToUse.setState(GHIssue.STATE_OPEN);
                try {
                    final SyncActionWrapper sync = ghIssueToUse.sync(syncEvent);
                    sync.sync();
                } catch (IOException e) {
                    throw new Error(e);
                }

            }
        });

        //making sure we tried to create the ACTask at least once
        verify(requestProcessor, times(1)).processPost(Mockito.any(ACTask.class), Mockito.anyString());

    }
}
