/**
 * 
 */
package pt.ist.maidSyncher.domain.offlineTests;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.egit.github.core.Milestone;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.maidSyncher.api.activeCollab.ACMilestone;
import pt.ist.maidSyncher.api.activeCollab.ACObject;
import pt.ist.maidSyncher.api.activeCollab.interfaces.RequestProcessor;
import pt.ist.maidSyncher.domain.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.dsi.DSIMilestone;
import pt.ist.maidSyncher.domain.github.GHMilestone;
import pt.ist.maidSyncher.domain.sync.EmptySyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncEvent;
import pt.ist.maidSyncher.domain.sync.SyncEvent.TypeOfChangeEvent;
import pt.ist.maidSyncher.domain.test.utils.OfflineSyncTests;
import pt.ist.maidSyncher.domain.test.utils.TestUtils;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 29 de Abr de 2013
 * 
 *         Tests the synching {@link GHMilestone#sync(SyncEvent)} method/functionality
 */
@Category(OfflineSyncTests.class)
@RunWith(MockitoJUnitRunner.class)
public class GHMilestoneSyncTest {

    GHMilestone ghMilestone;

    private final static String GH_MILESTONE_TITLE = "gh milestone title";
    private final static String GH_MILESTONE_DESCRIPTION = "gh milestone description";
    private final static DateTime GH_MILESTONE_DUE_ON_DT = new DateTime();

    private static final long AC_PROJECT_ID = 2;

    DSIMilestone dsiMilestone;

    @Mock
    private static RequestProcessor requestProcessor;

    @Before
    @Atomic
    public void init() {
        TestUtils.clearInstancesWithRoot();
        ghMilestone = new GHMilestone();
//        ghMilestone = mock(GHMilestone.class);
//        when(ghMilestone.getTitle()).thenReturn(GH_MILESTONE_TITLE);
//        when(ghMilestone.getDescription()).thenReturn(GH_MILESTONE_DESCRIPTION);
//        when(ghMilestone.getDueOn()).thenReturn(GH_MILESTONE_DUE_ON_LT);

        ghMilestone.setTitle(GH_MILESTONE_TITLE);
        ghMilestone.setDescription(GH_MILESTONE_DESCRIPTION);
        ghMilestone.setDueOn(GH_MILESTONE_DUE_ON_DT);

        pt.ist.maidSyncher.domain.activeCollab.ACMilestone acMilestoneOne =
                new pt.ist.maidSyncher.domain.activeCollab.ACMilestone();

        pt.ist.maidSyncher.domain.activeCollab.ACMilestone acMilestoneTwo =
                new pt.ist.maidSyncher.domain.activeCollab.ACMilestone();
        acMilestoneOne.setId(1);
        acMilestoneOne.setUrl("http://something/something");
        acMilestoneTwo.setId(2);
        acMilestoneTwo.setUrl("http://something/something");

        dsiMilestone = new DSIMilestone();
        ghMilestone.setDsiObjectMilestone(dsiMilestone);
        dsiMilestone.addAcMilestones(acMilestoneOne);
        dsiMilestone.addAcMilestones(acMilestoneTwo);

        //let's create the acproject to be associated with the milestones
        ACProject acProject = new ACProject();
        acProject.addMilestones(acMilestoneTwo);
        acProject.addMilestones(acMilestoneOne);
        acProject.setId(AC_PROJECT_ID);

//        ghMilestone.setTitle(GH_MILESTONE_TITLE);
//        ghMilestone.setDescription(GH_MILESTONE_DESCRIPTION);
//        ghMilestone.setDueOn(GH_MILESTONE_DUE_ON_LT);

    }

    @Captor
    ArgumentCaptor<ACMilestone> acMilestoneCaptor;

    @SuppressWarnings("static-access")
    @Test
    @Atomic(mode = TxMode.WRITE)
    public void update() throws IOException {

        SyncEvent updateSyncEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.UPDATE, this.ghMilestone,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(Milestone.class)));

        ACObject.setRequestProcessor(requestProcessor);
        JSONObject mockJsonObject = mock(JSONObject.class);
        when(mockJsonObject.get("id")).thenReturn(1, 2);
        when(mockJsonObject.get("project_id")).thenReturn(AC_PROJECT_ID);
        when(requestProcessor.processPost(Mockito.any(ACObject.class), Mockito.anyString())).thenReturn(mockJsonObject);

        SyncActionWrapper sync = this.ghMilestone.sync(updateSyncEvent);
        sync.sync();

        verify(requestProcessor, times(2)).processPost(acMilestoneCaptor.capture(), Mockito.anyString());
        for (ACMilestone acMilestone : acMilestoneCaptor.getAllValues()) {

            assertEquals(GH_MILESTONE_DESCRIPTION, acMilestone.getBody());
            assertEquals(GH_MILESTONE_TITLE, acMilestone.getName());
            assertEquals(GH_MILESTONE_DUE_ON_DT.toDate(), acMilestone.getDueOn());
        }

    }

    @Test
    @Atomic(mode = TxMode.WRITE)
    public void create() throws IOException {
        SyncEvent createSyncEvent = TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, this.ghMilestone, new Milestone());

        ACObject.setRequestProcessor(requestProcessor);

        SyncActionWrapper syncActionWrapper = ghMilestone.sync(createSyncEvent);

        syncActionWrapper.sync();

        verify(requestProcessor, never()).processPost(Mockito.any(ACObject.class), Mockito.anyString());
        verify(requestProcessor, never()).processPost(Mockito.anyString(), Mockito.anyString());
        verify(requestProcessor, never()).processGet(Mockito.anyString());
        assertEquals(EmptySyncActionWrapper.class, syncActionWrapper.getClass());
    }

}
