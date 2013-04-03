/**
 * 
 */
package pt.ist.maidSyncher.changesBuzz.sync.mock;

import java.io.IOException;

import org.json.simple.JSONObject;

import pt.ist.maidSyncher.api.activeCollab.ACObject;
import pt.ist.maidSyncher.api.activeCollab.interfaces.RequestProcessor;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 3 de Abr de 2013
 *
 * 
 */
public class MockRequestProcessor implements RequestProcessor {

    /**
     * 
     */
    public MockRequestProcessor() {
    }

    /* (non-Javadoc)
     * @see pt.ist.maidSyncher.api.activeCollab.interfaces.RequestProcessor#processGet(java.lang.String)
     */
    @Override
    public Object processGet(String _url) throws IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see pt.ist.maidSyncher.api.activeCollab.interfaces.RequestProcessor#processPost(pt.ist.maidSyncher.api.activeCollab.ACObject, java.lang.String)
     */
    @Override
    public JSONObject processPost(ACObject acObject, String relativePath) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see pt.ist.maidSyncher.api.activeCollab.interfaces.RequestProcessor#getBasicUrlForPath(java.lang.String)
     */
    @Override
    public String getBasicUrlForPath(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see pt.ist.maidSyncher.api.activeCollab.interfaces.RequestProcessor#processPost(java.lang.String, java.lang.String)
     */
    @Override
    public JSONObject processPost(String path, String string) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
