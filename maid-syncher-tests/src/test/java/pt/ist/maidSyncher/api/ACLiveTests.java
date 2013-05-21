/**
 * 
 */
package pt.ist.maidSyncher.api;

import java.io.IOException;
import java.util.Properties;

import org.junit.BeforeClass;

import pt.ist.maidSyncher.api.activeCollab.ACContext;

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
        acContext.setServer(acConfigurationProperties.getProperty("ac.server.host"));
        acContext.setToken(acConfigurationProperties.getProperty("ac.server.token"));

    }

}
