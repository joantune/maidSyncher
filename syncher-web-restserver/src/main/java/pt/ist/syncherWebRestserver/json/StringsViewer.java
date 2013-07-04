/**
 * 
 */
package pt.ist.syncherWebRestserver.json;

import java.util.List;

import pt.ist.bennu.core.annotation.DefaultJsonAdapter;
import pt.ist.bennu.json.JsonBuilder;
import pt.ist.bennu.json.JsonViewer;
import pt.utl.ist.fenix.tools.util.Strings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 2 de Jul de 2013
 *
 * 
 */
@DefaultJsonAdapter(Strings.class)
public class StringsViewer implements JsonViewer<Strings> {


    /* (non-Javadoc)
     * @see pt.ist.bennu.json.JsonViewer#view(java.lang.Object, pt.ist.bennu.json.JsonBuilder)
     */
    @Override
    public JsonElement view(Strings stringsObj, JsonBuilder ctx) {
        if (stringsObj == null)
            return null;
        final JsonArray jsonArray = new JsonArray();
        List<String> unmodifiableList = stringsObj.getUnmodifiableList();
        for (String string : unmodifiableList) {
            jsonArray.add(new JsonPrimitive(string));
        }

        return jsonArray;
    }

}
