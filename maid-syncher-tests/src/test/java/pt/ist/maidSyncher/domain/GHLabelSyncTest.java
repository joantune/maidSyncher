/**
 * 
 */
package pt.ist.maidSyncher.domain;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.egit.github.core.Label;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.maidSyncher.api.activeCollab.interfaces.RequestProcessor;
import pt.ist.maidSyncher.domain.dsi.DSIProject;
import pt.ist.maidSyncher.domain.github.GHLabel;
import pt.ist.maidSyncher.domain.github.GHMilestone;
import pt.ist.maidSyncher.domain.sync.EmptySyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.sync.SyncEvent;
import pt.ist.maidSyncher.domain.sync.SyncEvent.TypeOfChangeEvent;
import pt.ist.maidSyncher.domain.test.utils.TestUtils;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 29 de Abr de 2013
 * 
 *         Tests the synching {@link GHMilestone#sync(SyncEvent)} method/functionality
 */
@RunWith(MockitoJUnitRunner.class)
public class GHLabelSyncTest {

    GHLabel ghLabel;

    private final static String GH_LABEL_TITLE = "gh milestone title";
    private final static String GH_LABEL_COLOR = "smthing";

    private static final long AC_PROJECT_ID = 2;

    DSIProject dsiProject;

    @Mock
    private static RequestProcessor requestProcessor;

    @Before
    @Atomic
    public void init() {
        TestUtils.clearInstancesWithRoot();
        ghLabel = new GHLabel();

        ghLabel.setName(GH_LABEL_TITLE);
        ghLabel.setColor(GH_LABEL_COLOR);

        dsiProject = new DSIProject();
        dsiProject.addGitHubLabels(ghLabel);

    }

    @SuppressWarnings("static-access")
    @Test
    @Atomic(mode = TxMode.WRITE)
    public void update() throws IOException {

        SyncEvent updateSyncEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.UPDATE, this.ghLabel,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(Label.class)));

        SyncActionWrapper syncActionWrapper = ghLabel.sync(updateSyncEvent);

        assertTrue(syncActionWrapper instanceof EmptySyncActionWrapper);

    }

    @Test
    @Atomic(mode = TxMode.WRITE)
    public void create() throws IOException {
        SyncEvent createSyncEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, this.ghLabel,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(Label.class)));

        SyncActionWrapper syncActionWrapper = ghLabel.sync(createSyncEvent);

        assertTrue(syncActionWrapper instanceof EmptySyncActionWrapper);
    }

    public void delete() {
        //TODO
    }


}
