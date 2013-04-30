/**
 * 
 */
package pt.ist.maidSyncher.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.joda.time.LocalTime;
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
import pt.ist.maidSyncher.domain.SyncEvent.TypeOfChangeEvent;
import pt.ist.maidSyncher.domain.activeCollab.ACMilestone;
import pt.ist.maidSyncher.domain.dsi.DSIMilestone;
import pt.ist.maidSyncher.domain.github.GHMilestone;
import pt.ist.maidSyncher.domain.github.GHRepository;
import pt.ist.maidSyncher.domain.github.GHUser;
import pt.ist.maidSyncher.domain.sync.SyncActionWrapper;
import pt.ist.maidSyncher.domain.test.utils.TestUtils;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 29 de Abr de 2013
 * 
 *         Tests the synching {@link ACMilestone#sync(SyncEvent)} method/functionality
 */
@RunWith(MockitoJUnitRunner.class)
public class ACMilestoneSyncTest {

    ACMilestone acMilestone;

    private final static String AC_MILESTONE_NAME = "ac milestone name";
    private final static String AC_MILESTONE_BODY = "ac milestone body";
    private final static LocalTime AC_MILESTONE_DUE_ON_LT = new LocalTime();

    private static final String GH_REPOSITORY_NAME = "testRepo";

    private static final String GH_USER_NAME = "testUser";

    private static final long GH_REPOSITORY_ID = 5;

    DSIMilestone dsiMilestone;


    @Before
    @Atomic
    public void init() throws IOException {
        TestUtils.clearInstancesWithRoot();
        acMilestone = new ACMilestone();
//        ghMilestone = mock(GHMilestone.class);
//        when(ghMilestone.getTitle()).thenReturn(GH_MILESTONE_TITLE);
//        when(ghMilestone.getDescription()).thenReturn(GH_MILESTONE_DESCRIPTION);
//        when(ghMilestone.getDueOn()).thenReturn(GH_MILESTONE_DUE_ON_LT);

        acMilestone.setName(AC_MILESTONE_NAME);
        acMilestone.setBody(AC_MILESTONE_BODY);
        acMilestone.setDueOn(AC_MILESTONE_DUE_ON_LT);


        GHMilestone ghMilestoneOne = new GHMilestone();
        ghMilestoneOne.setId(1);
        ghMilestoneOne.setNumber(1);
        ghMilestoneOne.setUrl("http://something/something");

        GHMilestone ghMilestoneTwo = new GHMilestone();
        ghMilestoneTwo.setNumber(2);
        ghMilestoneTwo.setUrl("http://something/something");

        dsiMilestone = new DSIMilestone();
        acMilestone.setDsiObjectMilestone(dsiMilestone);
        dsiMilestone.addGhMilestones(ghMilestoneOne);
        dsiMilestone.addGhMilestones(ghMilestoneTwo);

        GHRepository ghRepository = new GHRepository();
        GHUser ghUser = new GHUser();
        ghUser.setLogin(GH_USER_NAME);
        ghRepository.setOwner(ghUser);
        ghRepository.setName(GH_REPOSITORY_NAME);
        ghRepository.addMilestones(ghMilestoneTwo);
        ghRepository.addMilestones(ghMilestoneOne);
        ghRepository.setId(GH_REPOSITORY_ID);

        MaidRoot.setGitHubClient(gitHubClient);

        //let's create the repository to be associated with the milestones
//        ACProject acProject = new ACProject();
//        acProject.addMilestones(ghMilestoneTwo);
//        acProject.addMilestones(ghMilestoneOne);
//        acProject.setId(AC_PROJECT_ID);

//        ghMilestone.setTitle(GH_MILESTONE_TITLE);
//        ghMilestone.setDescription(GH_MILESTONE_DESCRIPTION);
//        ghMilestone.setDueOn(GH_MILESTONE_DUE_ON_LT);

    }

    @Mock
    GitHubClient gitHubClient;

    @Mock
    GitHubResponse response;

    @Captor
    ArgumentCaptor<Milestone> milestoneCaptor;

    @Captor
    ArgumentCaptor<String> stringPostUriCaptor;

    @SuppressWarnings("static-access")
    @Test
    @Atomic(mode = TxMode.WRITE)
    public void update() throws IOException {

        when(gitHubClient.post(Mockito.anyString(), Mockito.any(Milestone.class), Mockito.any(Type.class))).thenAnswer(
                new Answer<Milestone>() {

                    @Override
                    public Milestone answer(InvocationOnMock invocation) throws Throwable {
                        Milestone milestoneArg = (Milestone) invocation.getArguments()[1];
                        return milestoneArg;
                    }
                });
        SyncEvent updateSyncEvent =
                TestUtils.syncEventGenerator(TypeOfChangeEvent.UPDATE, this.acMilestone, Arrays.asList(PropertyUtils
                        .getPropertyDescriptors(pt.ist.maidSyncher.api.activeCollab.ACMilestone.class)));

        SyncActionWrapper sync = this.acMilestone.sync(updateSyncEvent);
        sync.sync();

//        verify(gitHubClient).post(Mockito.eq("repos/" + GH_USER_NAME + "/" + GH_REPOSITORY_NAME + "/milestones/1"),
//                milestoneCaptor.capture(), Mockito.any(Type.class));
        verify(gitHubClient, times(2)).post(stringPostUriCaptor.capture(),
                milestoneCaptor.capture(), Mockito.any(Type.class));

        List<String> postUriValues = stringPostUriCaptor.getAllValues();
        assertEquals(2, postUriValues.size());

        for (String postUri : postUriValues) {
            assertTrue(postUri.equalsIgnoreCase("/repos/" + GH_USER_NAME + "/" + GH_REPOSITORY_NAME + "/milestones/1")
                    || postUri.equalsIgnoreCase("/repos/" + GH_USER_NAME + "/" + GH_REPOSITORY_NAME + "/milestones/2"));
        }
        assertTrue(postUriValues.get(0).equals(postUriValues.get(1)) == false);

        for (Milestone milestoneCaptured : milestoneCaptor.getAllValues()) {
            assertEquals(AC_MILESTONE_NAME, milestoneCaptured.getTitle());
            assertEquals(AC_MILESTONE_BODY, milestoneCaptured.getDescription());
            assertEquals(AC_MILESTONE_DUE_ON_LT.toDateTimeToday().toDate(), milestoneCaptured.getDueOn());
        }

    }

    @Test
    public void create() throws IOException {
        SyncEvent createSyncEvent = TestUtils.syncEventGenerator(TypeOfChangeEvent.CREATE, acMilestone);
        when(gitHubClient.get(Mockito.any(GitHubRequest.class))).thenReturn(response);

        SyncActionWrapper syncActionWrapper = acMilestone.sync(createSyncEvent);
        syncActionWrapper.sync();

        verify(gitHubClient, never()).get(Mockito.any(GitHubRequest.class));
        verify(gitHubClient, never()).post(Mockito.anyString());
        verify(gitHubClient, never()).post(Mockito.anyString(), Mockito.any(Object.class),
                Mockito.any(java.lang.reflect.Type.class));
        verify(gitHubClient, never()).postStream(Mockito.anyString(), Mockito.any(Object.class));
    }

}
