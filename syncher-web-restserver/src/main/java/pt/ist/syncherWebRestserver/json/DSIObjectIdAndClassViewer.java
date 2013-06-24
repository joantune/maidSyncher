/**
 * 
 */
package pt.ist.syncherWebRestserver.json;

import pt.ist.bennu.json.JsonBuilder;
import pt.ist.bennu.json.JsonViewer;
import pt.ist.maidSyncher.domain.dsi.DSIObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 21 de Jun de 2013
 *
 * 
 */
public class DSIObjectIdAndClassViewer implements JsonViewer<DSIObject> {


    /* (non-Javadoc)
     * @see pt.ist.bennu.json.JsonViewer#view(java.lang.Object, pt.ist.bennu.json.JsonBuilder)
     */
    @Override
    public JsonElement view(DSIObject obj, JsonBuilder ctx) {
        if (obj == null)
            return null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", obj.getExternalId());
        jsonObject.addProperty("type", obj.getClass().getSimpleName());
        return jsonObject;
    }

}
