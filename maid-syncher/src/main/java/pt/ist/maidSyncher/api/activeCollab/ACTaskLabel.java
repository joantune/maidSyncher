/**
 * 
 */
package pt.ist.maidSyncher.api.activeCollab;

import java.io.IOException;

import org.json.simple.JSONObject;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 22 de Fev de 2013
 *
 * 
 */

public class ACTaskLabel extends ACObject {

    private String name;
    private boolean isDefault;

    public ACTaskLabel(JSONObject jsonObj) throws IOException {
        super(jsonObj);
    }

    @Override
    protected void init(JSONObject jsonObject) throws IOException {
        setName(JsonRest.getString(jsonObject, "name"));
        setDefault(JsonRest.getBooleanFromString(jsonObject, "is_default"));
    }

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

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

}
