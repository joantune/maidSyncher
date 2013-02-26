/**
 * 
 */
package pt.ist.maidSyncher.api.activeCollab;

import java.io.IOException;

import org.json.simple.JSONObject;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 21 de Fev de 2013
 *
 * 
 */
public class ACProjectLabel extends ACObject {
    private String name;
    private Boolean defaultLabel;

    public ACProjectLabel(JSONObject jsonObj) throws IOException {
        super(jsonObj);
    }

    /* (non-Javadoc)
     * @see pt.ist.maidSyncher.api.activeCollab.ACObject#init(org.json.simple.JSONObject)
     */
    @Override
    protected void init(JSONObject jsonObject) throws IOException {
        setName(JsonRest.getString(jsonObject, "name"));
        setDefaultLabel(JsonRest.getBooleanFromString(jsonObject, "is_default"));
    }


    /* (non-Javadoc)
     * @see pt.ist.maidSyncher.api.activeCollab.ACObject#toJSONString()
     */
    @Override
    public String toJSONString() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getDefaultLabel() {
        return defaultLabel;
    }

    public void setDefaultLabel(Boolean defaultLabel) {
        this.defaultLabel = defaultLabel;
    }

}
