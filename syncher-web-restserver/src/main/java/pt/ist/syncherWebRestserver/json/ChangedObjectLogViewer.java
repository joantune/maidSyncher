/**
 * 
 */
package pt.ist.syncherWebRestserver.json;

import pt.ist.bennu.core.annotation.DefaultJsonAdapter;
import pt.ist.bennu.json.JsonBuilder;
import pt.ist.bennu.json.JsonViewer;
import pt.ist.maidSyncher.domain.sync.logs.ChangedObjectLog;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 21 de Ago de 2013
 *
 * 
 */
@DefaultJsonAdapter(ChangedObjectLogViewer.class)
public class ChangedObjectLogViewer implements JsonViewer<ChangedObjectLog> {


    /* (non-Javadoc)
     * @see pt.ist.bennu.json.JsonViewer#view(java.lang.Object, pt.ist.bennu.json.JsonBuilder)
     */
    @Override
    public JsonElement view(ChangedObjectLog obj, JsonBuilder ctx) {
        if (obj == null)
            return null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("className", obj.getClassName());
        jsonObject.addProperty("urlObject", obj.getUrlObject());
        return jsonObject;
    }

}
