package pt.ist.maidSyncher.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.junit.After;

import pt.ist.maidSyncher.api.activeCollab.ACProject;

public class ACProjectTest extends ACLiveTests {

//    private static final String AC_PROJECT_TEST_OVERVIEW = "ac project test overview";
//    private static final String AC_TEST_PROJECT_CREATION_NAME = "ac test project creation";
    private static final String AC_TEST_PROJECT_CREATION_NAME = "ac test project";

    private static ACProject createdACProject;

    public void createProject() throws IOException {
        ACProject acProject = new ACProject();
        acProject.setName(AC_TEST_PROJECT_CREATION_NAME);
//        acProject.setOverview(AC_PROJECT_TEST_OVERVIEW);

        createdACProject = ACProject.create(acProject);

        assertTrue(StringUtils.isNotBlank(createdACProject.getUrl()));
        assertTrue(createdACProject.getId() > 0);
        assertEquals(AC_TEST_PROJECT_CREATION_NAME, createdACProject.getName());
//        assertEquals(AC_PROJECT_TEST_OVERVIEW, createdACProject.getOverview());


    }

    @After
    public void deleteProject() {
        if (createdACProject != null) {
            //nothing to do here (can't delete [?! maybe a is_deleted 1 post..])
        }
    }


}
