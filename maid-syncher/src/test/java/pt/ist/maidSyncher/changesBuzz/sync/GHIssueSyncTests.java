/**
 * 
 */
package pt.ist.maidSyncher.changesBuzz.sync;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.Collections;

import jvstm.TransactionalCommand;

import org.joda.time.LocalTime;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import pt.ist.fenixframework.pstm.Transaction;
import pt.ist.maidSyncher.api.activeCollab.ACObject;
import pt.ist.maidSyncher.api.activeCollab.ACTask;
import pt.ist.maidSyncher.api.activeCollab.interfaces.RequestProcessor;
import pt.ist.maidSyncher.changesBuzz.FFTest;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SyncEvent;
import pt.ist.maidSyncher.domain.SyncEvent.SyncUniverse;
import pt.ist.maidSyncher.domain.SyncEvent.TypeOfChangeEvent;
import pt.ist.maidSyncher.domain.SynchableObject;
import pt.ist.maidSyncher.domain.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.activeCollab.ACTaskCategory;
import pt.ist.maidSyncher.domain.dsi.DSIIssue;
import pt.ist.maidSyncher.domain.dsi.DSIRepository;
import pt.ist.maidSyncher.domain.exceptions.SyncEventOriginObjectChanged;
import pt.ist.maidSyncher.domain.github.GHIssue;
import pt.ist.maidSyncher.domain.github.GHLabel;
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

                createSyncEvent = syncEventGenerator(TypeOfChangeEvent.CREATE, ghIssueToUse);
                updateSyncEvent = syncEventGenerator(TypeOfChangeEvent.UPDATE, ghIssueToUse);

            }
        });
    }

    private SyncEvent syncEventGenerator(final TypeOfChangeEvent typeOfChangeEvent, final SynchableObject originObject) {
        return new SyncEvent(new LocalTime(), typeOfChangeEvent, Collections.<PropertyDescriptor> emptySet(), null,
                new APIObjectWrapper() {

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

    @Test
    public void createOpenGHIssueWithoutMilestone() throws Exception {

        final String ghIssueTitle = "Test issue";
        final String ghDescription = "Test on the description, this must be the same";
        when(requestProcessor.processPost(Mockito.any(ACObject.class), Mockito.anyString())).then(new Answer<JSONObject>() {

            @Override
            public JSONObject answer(InvocationOnMock invocation) throws Throwable {
                ACTask acTask = (ACTask) invocation.getArguments()[0];
                //Verify that the name is the same
                assertEquals(acTask.getName(), ghIssueTitle);
                assertEquals(acTask.getBody(), ghDescription);
                return new JSONObject();
            }
        });
        ACObject.setRequestProcessor(requestProcessor);

        Transaction.withTransaction(new TransactionalCommand() {

            @Override
            public void doIt() {
                //let us say that the issue is open
                ghIssueToUse.setState(GHIssue.STATE_OPEN);
                ghIssueToUse.setTitle(ghIssueTitle);
                ghIssueToUse.setBodyHtml(ghDescription);

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
        acTask.setProject(acProject);
    }
}
