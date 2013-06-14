/**
 * 
 */
package pt.ist.syncherWebRestserver.json;

import org.joda.time.LocalDateTime;

import pt.ist.bennu.core.annotation.DefaultJsonAdapter;
import pt.ist.bennu.json.JsonBuilder;
import pt.ist.bennu.json.JsonViewer;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 14 de Jun de 2013
 *
 * 
 */
@DefaultJsonAdapter(LocalDateTime.class)
public class LocalDateTimeViewer implements JsonViewer<LocalDateTime> {

    /* (non-Javadoc)
     * @see pt.ist.bennu.json.JsonViewer#view(java.lang.Object, pt.ist.bennu.json.JsonBuilder)
     */
    @Override
    public JsonElement view(LocalDateTime obj, JsonBuilder ctx) {
        if (obj == null) {
            return null;
        }
        return new JsonPrimitive(obj.toString("MMMM dd YYYY, HH:mm:ss:S"));
    }

}
