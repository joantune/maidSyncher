/**
 * 
 */
package pt.ist.maidSyncher.domain;

import java.io.IOException;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import pt.ist.fenixframework.Atomic;
import pt.ist.maidSyncher.domain.activeCollab.ACProject;
import pt.ist.maidSyncher.domain.activeCollab.ACTaskCategory;
import pt.ist.maidSyncher.domain.dsi.DSIProject;
import pt.ist.maidSyncher.domain.dsi.DSIRepository;
import pt.ist.maidSyncher.domain.github.GHMilestone;
import pt.ist.maidSyncher.domain.github.GHRepository;
import pt.ist.maidSyncher.domain.sync.SyncEvent;
import pt.ist.maidSyncher.domain.test.utils.OfflineSyncTests;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 29 de Abr de 2013
 * 
 *         Tests the synching {@link GHMilestone#sync(SyncEvent)} method/functionality
 */
@Category(OfflineSyncTests.class)
@RunWith(MockitoJUnitRunner.class)
public class GHRepositorySyncTestsBase {

    protected static final String GH_REPOSITORY_ALTERNATIVE_NAME = "gh repository alternative name";

    protected static final String GH_REPOSITORY_NAME = "gh repository name";

    protected static final String AC_PROJECT_TWO = "AC project two";
    protected static final String AC_PROJECT_ONE = "AC project one";

    protected GHRepository ghRepository;

    protected DSIRepository dsiRepository;

    protected ACProject acDefaultProject;

    protected ACProject acProjectOne;

    protected ACProject acProjectTwo;

    protected DSIProject dsiProjectOne;

    protected DSIProject dsiProjectTwo;

    protected DSIProject dsiDefaultProject;

    protected ACTaskCategory acTaskCategoryOfProjectOne;

    protected ACTaskCategory acTaskCategoryOfProjectTwo;

    protected ACTaskCategory acTaskCategoryOfDefaultProject;




    @Atomic
    public void init() throws IOException {

        ghRepository = new GHRepository();
        dsiRepository = new DSIRepository();

        ghRepository.setName(GH_REPOSITORY_NAME);
        ghRepository.setDsiObjectRepository(dsiRepository);


        dsiProjectOne = new DSIProject();
        dsiProjectTwo = new DSIProject();



    }

    private static final String CATEGORIES_END_URI = "/tasks/categories";

    protected void initDefaultProject() {
        dsiDefaultProject = new DSIProject();
    }

}
