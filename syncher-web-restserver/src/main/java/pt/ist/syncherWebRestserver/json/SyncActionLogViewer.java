/**
 * 
 */
package pt.ist.syncherWebRestserver.json;

import pt.ist.bennu.core.annotation.DefaultJsonAdapter;
import pt.ist.bennu.core.rest.json.DomainObjectViewer;
import pt.ist.bennu.json.JsonBuilder;
import pt.ist.bennu.json.JsonViewer;
import pt.ist.maidSyncher.domain.sync.logs.SyncActionLog;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 14 de Jun de 2013
 *
 * 
 */
@DefaultJsonAdapter(SyncActionLog.class)
public class SyncActionLogViewer implements JsonViewer<SyncActionLog> {


    /* (non-Javadoc)
     * @see pt.ist.bennu.json.JsonViewer#view(java.lang.Object, pt.ist.bennu.json.JsonBuilder)
     */
    @Override
    public JsonElement view(SyncActionLog obj, JsonBuilder ctx) {
        if (obj == null)
            return null;
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("id", obj.getExternalId());

        jsonObject.add("syncLog", ctx.view(obj.getSyncLog(), DomainObjectViewer.class));

        jsonObject.add("syncStartTime", ctx.view(obj.getSyncStartTime()));
        jsonObject.add("syncEndTime", ctx.view(obj.getSyncEndTime()));
        jsonObject.addProperty("success", obj.getSuccess());
        jsonObject.addProperty("urlOriginObject", obj.getUrlOriginObject());
        jsonObject.addProperty("errorDescription", obj.getErrorDescription());
        jsonObject.addProperty("actionDescription", obj.getActionDescription());
        jsonObject.addProperty("typeOriginObject", obj.getTypeOriginObject());
        jsonObject.add("typeOfChangeEvent", ctx.view(obj.getTypeOfChangeEvent()));
        jsonObject.add("changedDescriptors", ctx.view(obj.getChangedDescriptors()));

        jsonObject.add("dsiObject", ctx.view(obj.getDsiObject(), DSIObjectIdAndClassViewer.class));
        return jsonObject;
    }

}
