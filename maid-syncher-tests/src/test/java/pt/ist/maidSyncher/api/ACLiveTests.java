/**
 * 
 */
package pt.ist.maidSyncher.api;

import java.io.IOException;
import java.util.Properties;

import org.junit.BeforeClass;

import pt.ist.maidSyncher.api.activeCollab.ACContext;
import pt.ist.maidSyncher.domain.MaidRoot;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 21 de Mai de 2013
 * 
 *         Superclass for live tests, with the initialization methods for all of the livetests
 *         for Active Collab API classes
 */
public class ACLiveTests {

    @BeforeClass
    public static void init() throws IOException {
        // setup ActiveCollab
        Properties acConfigurationProperties = new Properties();
        acConfigurationProperties.load(ACLiveTests.class.getResourceAsStream("/configuration.properties"));

        ACContext acContext = ACContext.getInstance();
        acContext.setServerBaseUrl(acConfigurationProperties.getProperty(MaidRoot.AC_SERVER_BASE_URL));
        acContext.setToken(acConfigurationProperties.getProperty("ac.server.token"));

    }

}
