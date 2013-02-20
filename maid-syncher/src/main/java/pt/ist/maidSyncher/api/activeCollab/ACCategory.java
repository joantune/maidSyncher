/**
 * 
 */
package pt.ist.maidSyncher.api.activeCollab;

import java.io.IOException;

import org.json.simple.JSONObject;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 13 de Fev de 2013
 * 
 *         It represents an Active Collab category
 */
public class ACCategory extends ACObject {

    private String name;

    public ACCategory(JSONObject jsonObject) throws IOException {
        super(jsonObject);

    }

    @Override
    protected void init(JSONObject jsonObject) throws IOException {
        setName(JsonRest.getString(jsonObject, "name"));

    }

    /* (non-Javadoc)
     * @see pt.ist.maidSyncher.api.activeCollab.ACObject#toJSONString()
     */
    @Override
    public String toJSONString() {
        // TODO
        throw new Error("TODO");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
