/**
 * 
 */
package pt.ist.maidSyncher.changesBuzz.sync;

import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;

import jvstm.TransactionalCommand;

import org.joda.time.LocalTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import pt.ist.fenixframework.pstm.Transaction;
import pt.ist.maidSyncher.api.activeCollab.ACMilestone;
import pt.ist.maidSyncher.api.activeCollab.ACTask;
import pt.ist.maidSyncher.changesBuzz.ChangesBuzz;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.SyncEvent;
import pt.ist.maidSyncher.domain.SyncEvent.SyncUniverse;
import pt.ist.maidSyncher.domain.SyncEvent.TypeOfChangeEvent;
import pt.ist.maidSyncher.domain.activeCollab.ACProject;
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
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ACTask.class, ACMilestone.class })
public class GHIssueSyncTests extends ChangesBuzz {

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
    private static GHRepository mockGHRepository = mock(GHRepository.class);
    private static DSIRepository mockDSIRepository = mock(DSIRepository.class);

    private static DSIIssue mockDSIIssue = mock(DSIIssue.class);

    private static ACProject mockACProject = mock(ACProject.class);

    public void initMockGHIssue() {
        Transaction.withTransaction(new TransactionalCommand() {

            @Override
            public void doIt() {
                if (ghIssueToUse == null)
                {
                    //let's create a usable GHRepository
                    GHRepository ghRepository = new GHRepository();
                    GHUser ghUser = new GHUser();
                    ghRepository.setOwner(ghUser);
                    ghUser.setOrganization(MaidRoot.getInstance().getGhOrganization());

                    ghIssueToUse = spy(new GHIssue());
                    ghIssueToUse.setRepository(ghRepository);
                    when(ghIssueToUse.getRepository()).thenReturn(mockGHRepository);
                    when(mockGHRepository.getDsiObjectRepository()).thenReturn(mockDSIRepository);
                    when(ghIssueToUse.getDsiObjectIssue()).thenReturn(mockDSIIssue);
                    when(mockDSIRepository.getDefaultProject()).thenReturn(mockACProject);

                }

            }
        });
    }

    @Test
    public void createGHIssueWithoutMilestone() {
        initMockGHIssue();

        mockStatic(ACMilestone.class, new Answer<ACMilestone>() {

            @Override
            public ACMilestone answer(InvocationOnMock invocation) throws Throwable {
                Method method = invocation.getMethod();
                if (method.getName().equalsIgnoreCase("create")) {
                    return (ACMilestone) invocation.getArguments()[0];
                }
                return null;
            }
        });

        mockStatic(ACTask.class, new Answer<ACTask>() {

            @Override
            public ACTask answer(InvocationOnMock invocation) throws Throwable {
                ACTask acTask = (ACTask) invocation.getArguments()[0];
                acTask.setId(123);
                return acTask;
            }
        });

        Assert.assertEquals(mockGHRepository, ghIssueToUse.getRepository());

        SyncEvent syncEvent =
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

        SyncActionWrapper sync = ghIssueToUse.sync(syncEvent);

        try {
            sync.sync();
        } catch (IOException e) {
            throw new Error(e);
        }

        verify(ACMilestone.class, never());

        verify(ACTask.class, atMost(1));


    }
}
