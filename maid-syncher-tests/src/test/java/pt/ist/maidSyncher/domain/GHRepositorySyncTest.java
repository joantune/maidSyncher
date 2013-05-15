/**
 * 
 */
package pt.ist.maidSyncher.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.egit.github.core.Label;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.maidSyncher.api.activeCollab.ACCategory;
import pt.ist.maidSyncher.api.activeCollab.ACObject;
import pt.ist.maidSyncher.api.activeCollab.interfaces.RequestProcessor;
import pt.ist.maidSyncher.domain.SyncEvent.TypeOfChangeEvent;
import pt.ist.maidSyncher.domain.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.activeCollab.ACTaskCategory;
import pt.ist.maidSyncher.domain.dsi.DSIProject;
import pt.ist.maidSyncher.domain.dsi.DSIRepository;
import pt.ist.maidSyncher.domain.github.GHMilestone;
import pt.ist.maidSyncher.domain.github.GHRepository;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.test.utils.TestUtils;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 29 de Abr de 2013
 * 
 *         Tests the synching {@link GHMilestone#sync(SyncEvent)} method/functionality
 */
@RunWith(MockitoJUnitRunner.class)
public class GHRepositorySyncTest {

    private static final String GH_REPOSITORY_NAME = "gh repository name";

    GHRepository ghRepository;

    DSIRepository dsiRepository;

    ACProject acDefaultProject;

    ACProject acProjectOne;

    ACProject acProjectTwo;

    DSIProject dsiProjectOne;

    DSIProject dsiProjectTwo;

    DSIProject dsiDefaultProject;

    ACTaskCategory acTaskCategoryOfProjectOne;

    ACTaskCategory acTaskCategoryOfProjectTwo;

    ACTaskCategory acTaskCategoryOfDefaultProject;

    @Mock
    private static RequestProcessor requestProcessor;

    @Mock
    private static JSONObject mockJSONObject;

    @Captor
    ArgumentCaptor<ACObject> acObjectCaptor;

    @Captor
    ArgumentCaptor<String> pathCaptor;

    @Before
    @Atomic
    public void init() throws IOException {
        TestUtils.clearInstancesWithRoot();

        ghRepository = new GHRepository();
        dsiRepository = new DSIRepository();

        ghRepository.setName(GH_REPOSITORY_NAME);
        ghRepository.setDsiObjectRepository(dsiRepository);

        acProjectOne = new ACProject();
        acProjectTwo = new ACProject();

        dsiProjectOne = new DSIProject();
        dsiProjectTwo = new DSIProject();

        acProjectOne.setDsiObjectProject(dsiProjectOne);
        acProjectTwo.setDsiObjectProject(dsiProjectTwo);

        when(mockJSONObject.get("id")).thenReturn(123l);
        when(mockJSONObject.get("is_archived")).thenReturn(0);
        when(mockJSONObject.get("parent_class")).thenReturn(ACCategory.PROJECT_CLASS);
        when(requestProcessor.processPost(Mockito.any(ACObject.class), Mockito.anyString())).thenReturn(mockJSONObject);

        ACObject.setRequestProcessor(requestProcessor);

    }

    private static final String CATEGORIES_END_URI = "/tasks/categories";

    @SuppressWarnings("static-access")
    @Test
    @Atomic(mode = TxMode.WRITE)
    public void createWithoutReusableTaskCategoriesOrProject() throws IOException {

        SyncEvent updateSyncEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, this.ghRepository,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(Label.class)));

        SyncActionWrapper syncActionWrapper = ghRepository.sync(updateSyncEvent);

        syncActionWrapper.sync();

        //so, we should have two creates

        verify(requestProcessor, times(3)).processPost(acObjectCaptor.capture(), pathCaptor.capture());

//        for (String pathString : pathCaptor.getAllValues()) {
//            if (assert)
//            assertTrue(pathString.endsWith(CATEGORIES_END_URI));
//        }

        //now let's make sure that the categories posted are correct
        for (ACObject acObject : acObjectCaptor.getAllValues()) {
            if (acObject instanceof ACCategory) {
                ACCategory acCategory = (ACCategory) acObject;
                assertEquals(ACTaskCategory.REPOSITORY_PREFIX + GH_REPOSITORY_NAME, acCategory.getName());
            } else if (acObject instanceof pt.ist.maidSyncher.api.activeCollab.ACProject) {
                pt.ist.maidSyncher.api.activeCollab.ACProject acProjectCreated =
                        (pt.ist.maidSyncher.api.activeCollab.ACProject) acObject;
                assertEquals(GH_REPOSITORY_NAME, acProjectCreated.getName());

            } else
                fail();
        }


    }

    @Test
    @Atomic(mode = TxMode.WRITE)
    public void createWithReusableTaskCategoryAndProject() throws IOException {

        initDefaultProject();
        initTaskCategories();

        SyncEvent updateSyncEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, this.ghRepository,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(Label.class)));

        SyncActionWrapper syncActionWrapper = ghRepository.sync(updateSyncEvent);

        syncActionWrapper.sync();

        //so, we should have two creates

        verify(requestProcessor, never()).processPost(Mockito.any(ACObject.class), Mockito.anyString());

//        for (String pathString : pathCaptor.getAllValues()) {
//            if (assert)
//            assertTrue(pathString.endsWith(CATEGORIES_END_URI));
//        }

        //now let's make sure that the categories posted are correct
//        for (ACObject acObject : acObjectCaptor.getAllValues()) {
//            if (acObject instanceof ACCategory) {
//                ACCategory acCategory = (ACCategory) acObject;
//                assertEquals(ACTaskCategory.REPOSITORY_PREFIX + GH_REPOSITORY_NAME, acCategory.getName());
//            } else if (acObject instanceof pt.ist.maidSyncher.api.activeCollab.ACProject) {
//                pt.ist.maidSyncher.api.activeCollab.ACProject acProjectCreated =
//                        (pt.ist.maidSyncher.api.activeCollab.ACProject) acObject;
//                assertEquals(GH_REPOSITORY_NAME, acProjectCreated.getName());
//
//            } else
//                fail();
//        }

    }

    private void initDefaultProject() {
        acDefaultProject = new ACProject();
        dsiDefaultProject = new DSIProject();
        acDefaultProject.setDsiObjectProject(dsiDefaultProject);
        acDefaultProject.setName(GH_REPOSITORY_NAME);

    }

    private void initTaskCategories() {
        acTaskCategoryOfProjectOne = new ACTaskCategory();

        acTaskCategoryOfProjectOne.setName(ACTaskCategory.REPOSITORY_PREFIX + GH_REPOSITORY_NAME);
        acTaskCategoryOfProjectOne.setProject(acProjectOne);

        acTaskCategoryOfProjectTwo = new ACTaskCategory();
        acTaskCategoryOfProjectTwo.setName(ACTaskCategory.REPOSITORY_PREFIX + GH_REPOSITORY_NAME);
        acTaskCategoryOfProjectTwo.setProject(acProjectTwo);

        acTaskCategoryOfDefaultProject = new ACTaskCategory();
        acTaskCategoryOfDefaultProject.setName(ACTaskCategory.REPOSITORY_PREFIX + GH_REPOSITORY_NAME);
        acTaskCategoryOfDefaultProject.setProject(acDefaultProject);

    }

    public void update() throws IOException {
    }


}
