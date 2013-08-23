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

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.egit.github.core.Repository;
import org.json.simple.JSONObject;
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
import pt.ist.maidSyncher.api.activeCollab.ACCategory;
import pt.ist.maidSyncher.api.activeCollab.ACObject;
import pt.ist.maidSyncher.api.activeCollab.interfaces.RequestProcessor;
import pt.ist.maidSyncher.domain.GHRepositorySyncTestsBase;
import pt.ist.maidSyncher.domain.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.activeCollab.ACTaskCategory;
import pt.ist.maidSyncher.domain.dsi.DSIRepository;
import pt.ist.maidSyncher.domain.github.GHMilestone;
import pt.ist.maidSyncher.domain.github.GHRepository;
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
public class GHRepositorySyncOfflineTest extends GHRepositorySyncTestsBase {


    @Mock
    private static RequestProcessor requestProcessor;

    @Mock
    private static JSONObject mockJSONObject;

    @Captor
    ArgumentCaptor<ACObject> acObjectCaptor;

    @Captor
    ArgumentCaptor<String> pathCaptor;
    final static Random randomIDGenerator = new Random();



    @Override
    @Before
    @Atomic
    public void init() throws IOException {
        TestUtils.clearInstancesWithRoot();
        super.init();

        acProjectOne = new ACProject();
        acProjectTwo = new ACProject();

        acProjectOne.setDsiObjectProject(dsiProjectOne);
        acProjectTwo.setDsiObjectProject(dsiProjectTwo);

        acProjectOne.setName("AC project one");
        acProjectTwo.setName("AC project two");

        acProjectOne.setId(1);
        acProjectTwo.setId(2);

        when(mockJSONObject.get("id")).thenReturn(randomIDGenerator.nextInt(12000));
        when(mockJSONObject.get("is_archived")).thenReturn(0);
        when(mockJSONObject.get("parent_class")).thenReturn(ACCategory.PROJECT_CLASS);
        when(requestProcessor.processPost(Mockito.any(ACObject.class), Mockito.anyString())).thenReturn(mockJSONObject);

        ACObject.setRequestProcessor(requestProcessor);

        acProjectOne.setId(1);
        acProjectTwo.setId(2);

    }

    private static final String CATEGORIES_END_URI = "/tasks/categories";

    @SuppressWarnings("static-access")
    @Test
    @Atomic(mode = TxMode.WRITE)
    public void createWithoutReusableTaskCategoriesOrProject() throws IOException {

        when(requestProcessor.getBasicUrlForPath(Mockito.anyString())).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[0];
            }
        });

        when(requestProcessor.processPost(Mockito.isA(ACCategory.class), Mockito.anyString())).then(new Answer<JSONObject>() {

            @Override
            public JSONObject answer(InvocationOnMock invocation) throws Throwable {
                ACCategory acCategory = (ACCategory) invocation.getArguments()[0];
                String uriString = (String) invocation.getArguments()[1];

                Integer projectId = Integer.valueOf(StringUtils.substringBetween(uriString, "projects/", "/tasks"));

                //let's return an appropriate random id
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", randomIDGenerator.nextInt(2000));
                jsonObject.put("is_archived", 0);
                jsonObject.put("parent_class", ACCategory.PROJECT_CLASS);
                jsonObject.put("parent_id", projectId);
                jsonObject.put("name", acCategory.getName());
                return jsonObject;

            }
        });

        SyncEvent updateSyncEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, this.ghRepository,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(Repository.class)));

        SyncActionWrapper syncActionWrapper = ghRepository.sync(updateSyncEvent);

        syncActionWrapper.sync();

        //so, we should have three creates of categories, and one create of project

        verify(requestProcessor, times(4)).processPost(acObjectCaptor.capture(), pathCaptor.capture());

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

        //let's make sure that all of the active projects have task categories for the
        //repository
        Set<ACProject> activeProjects = ACProject.getActiveProjects();
        assertEquals(3, activeProjects.size()); //one for each project and another for the default
        //that should have been made

        //now let's verify that each project has the needed task category
        for (ACProject acProject : activeProjects) {
            boolean foundTaskCategory = false;
            for (ACTaskCategory acTaskCategory : acProject.getTaskCategoriesDefinedSet()) {
                if (acTaskCategory.getName().equalsIgnoreCase(ACTaskCategory.REPOSITORY_PREFIX + GH_REPOSITORY_NAME)) {
                    foundTaskCategory = true;
                }
            }
            assertTrue("ACProject name: " + acProject.getName() + " nr task categories: "
                    + acProject.getTaskCategoriesDefinedSet().size(), foundTaskCategory);
        }

    }

    @SuppressWarnings("static-access")
    @Test
    @Atomic(mode = TxMode.WRITE)
    public void createWithPreviousRepositories() throws IOException {

        when(requestProcessor.getBasicUrlForPath(Mockito.anyString())).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[0];
            }
        });

        when(requestProcessor.processPost(Mockito.isA(ACCategory.class), Mockito.anyString())).then(new Answer<JSONObject>() {

            @Override
            public JSONObject answer(InvocationOnMock invocation) throws Throwable {
                ACCategory acCategory = (ACCategory) invocation.getArguments()[0];
                String uriString = (String) invocation.getArguments()[1];

                Integer projectId = Integer.valueOf(StringUtils.substringBetween(uriString, "projects/", "/tasks"));

                //let's return an appropriate random id
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", randomIDGenerator.nextInt(2000));
                jsonObject.put("is_archived", 0);
                jsonObject.put("parent_class", ACCategory.PROJECT_CLASS);
                jsonObject.put("parent_id", projectId);
                jsonObject.put("name", acCategory.getName());
                return jsonObject;

            }
        });

//let's create another repository
        GHRepository otherGhRepository = new GHRepository();
        DSIRepository otherDSIRepository = new DSIRepository();

        otherGhRepository.setName(GH_REPOSITORY_ALTERNATIVE_NAME);
        ghRepository.setDsiObjectRepository(otherDSIRepository);

        SyncEvent updateSyncEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, this.ghRepository,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(Repository.class)));

        SyncActionWrapper syncActionWrapper = ghRepository.sync(updateSyncEvent);

        syncActionWrapper.sync();

        //so, we should have four creates of categories.
        //one for the new GHRepository on the two existing projects, plus two for the
        //two existing repositories on the new default project. Plus the one for the create of the project


        verify(requestProcessor, times(5)).processPost(acObjectCaptor.capture(), pathCaptor.capture());

        //now let's make sure that the categories posted are correct
        int nrCategoryCreationsForNewRep = 0;
        int nrCategoryCreationsForAlternativeRep = 0;
        for (ACObject acObject : acObjectCaptor.getAllValues()) {
            if (acObject instanceof ACCategory) {
                ACCategory acCategory = (ACCategory) acObject;
                if (acCategory.getName().equals(ACTaskCategory.REPOSITORY_PREFIX + GH_REPOSITORY_NAME)) {
                    nrCategoryCreationsForNewRep++;
                } else if (acCategory.getName().equals(ACTaskCategory.REPOSITORY_PREFIX + GH_REPOSITORY_ALTERNATIVE_NAME)) {
                    nrCategoryCreationsForAlternativeRep++;
                }
            } else if (acObject instanceof pt.ist.maidSyncher.api.activeCollab.ACProject) {
                pt.ist.maidSyncher.api.activeCollab.ACProject acProjectCreated =
                        (pt.ist.maidSyncher.api.activeCollab.ACProject) acObject;
                assertEquals(GH_REPOSITORY_NAME, acProjectCreated.getName());

            } else
                fail();
        }

        assertEquals(3, nrCategoryCreationsForNewRep);
        assertEquals(1, nrCategoryCreationsForAlternativeRep);

        //let's make sure that all of the active projects have task categories for the
        //repository
        Set<ACProject> activeProjects = ACProject.getActiveProjects();
        assertEquals(3, activeProjects.size()); //one for each project and another for the default
        //that should have been made

        //now let's verify that each project has the needed task category
        for (ACProject acProject : activeProjects) {
            boolean foundTaskCategory = false;
            for (ACTaskCategory acTaskCategory : acProject.getTaskCategoriesDefinedSet()) {
                if (acTaskCategory.getName().equalsIgnoreCase(ACTaskCategory.REPOSITORY_PREFIX + GH_REPOSITORY_NAME)
                        || acTaskCategory.getName().equalsIgnoreCase(
                                ACTaskCategory.REPOSITORY_PREFIX + GH_REPOSITORY_ALTERNATIVE_NAME)) {
                    foundTaskCategory = true;
                } else {
                    foundTaskCategory = false;
                    break;
                }
            }
            assertTrue("ACProject name: " + acProject.getName() + " nr task categories: "
                    + acProject.getTaskCategoriesDefinedSet().size(), foundTaskCategory);
        }

    }

    @Test
    @Atomic(mode = TxMode.WRITE)
    public void createWithReusableTaskCategoryAndProject() throws IOException {

        initDefaultProject();
        initTaskCategories();

        SyncEvent createSyncEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, this.ghRepository,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(Repository.class)));

        SyncActionWrapper syncActionWrapper = ghRepository.sync(createSyncEvent);

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

    @Override
    protected void initDefaultProject() {
        super.initDefaultProject();
        acDefaultProject = new ACProject();
        acDefaultProject.setDsiObjectProject(dsiDefaultProject);
        acDefaultProject.setName(GH_REPOSITORY_NAME);

        acDefaultProject.setId(3);

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

        acTaskCategoryOfDefaultProject.setId(33);
        acTaskCategoryOfProjectOne.setId(11);
        acTaskCategoryOfProjectTwo.setId(22);

    }

    @Test
    @Atomic(mode = TxMode.WRITE)
    public void update() throws IOException {

        //setting up the stub
        when(requestProcessor.processPost(Mockito.any(ACObject.class), Mockito.anyString())).then(new Answer<JSONObject>() {

            @Override
            public JSONObject answer(InvocationOnMock invocation) throws Throwable {
                ACObject acObject = (ACObject) invocation.getArguments()[0];
                if (acObject instanceof ACCategory) {
                    ACCategory acCategory = (ACCategory) acObject;
                    when(mockJSONObject.get("parent_class")).thenReturn(ACCategory.PROJECT_CLASS);
                    when(mockJSONObject.get("parent_id")).thenReturn(acCategory.getProjectId());
                }
                return mockJSONObject;
            }

        });
        initDefaultProject();
        initTaskCategories();
        //let's connect them
        dsiRepository.setDefaultProject(acDefaultProject);
        dsiRepository.addAcTaskCategories(acTaskCategoryOfDefaultProject);
        dsiRepository.addAcTaskCategories(acTaskCategoryOfProjectOne);
        dsiRepository.addAcTaskCategories(acTaskCategoryOfProjectTwo);

        acDefaultProject.setId(321l);

        ghRepository.setName(GH_REPOSITORY_ALTERNATIVE_NAME);
        SyncEvent updateSyncEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.UPDATE, this.ghRepository,
                        Arrays.asList(PropertyUtils.getPropertyDescriptors(Repository.class)));

        SyncActionWrapper sync = ghRepository.sync(updateSyncEvent);

        sync.sync();

        //now, we should have requests to change the name of all the acTaskCategories and the default project as well
        verify(requestProcessor, times(4)).processPost(acObjectCaptor.capture(), pathCaptor.capture());

        boolean gotDefaultProjectPost = false;
        int nrTaskCategoryPosts = 0;
        for (ACObject acObject : acObjectCaptor.getAllValues()) {
            if (acObject instanceof pt.ist.maidSyncher.api.activeCollab.ACProject) {
                gotDefaultProjectPost = true;
                pt.ist.maidSyncher.api.activeCollab.ACProject acProject =
                        (pt.ist.maidSyncher.api.activeCollab.ACProject) acObject;
                assertEquals(321, acProject.getId());
                assertEquals(GH_REPOSITORY_ALTERNATIVE_NAME, acProject.getName());

            } else if (acObject instanceof ACCategory) {
                nrTaskCategoryPosts++;
                ACCategory acCategory = (ACCategory) acObject;
                assertEquals(ACTaskCategory.REPOSITORY_PREFIX + GH_REPOSITORY_ALTERNATIVE_NAME, acCategory.getName());

            } else {
                fail();
            }
        }

        assertTrue(gotDefaultProjectPost);
        assertEquals(3, nrTaskCategoryPosts);

    }

}
