package pt.ist.syncherWebRestserver.json;

import pt.ist.bennu.core.annotation.DefaultJsonAdapter;
import pt.ist.bennu.json.JsonBuilder;
import pt.ist.bennu.json.JsonViewer;
import pt.ist.maidSyncher.domain.SynchableObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * 
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 14 de Jun de 2013
 * 
 *         {@link SynchableObject} minimal viewer. It will only return the oid, the {@link SynchableObject} descriptors (DSC
 *         constants), and the classname
 */
@DefaultJsonAdapter(SynchableObject.class)
public class SynchableObjectDefaultMinimalViewer implements JsonViewer<SynchableObject> {

    @Override
    public JsonElement view(SynchableObject obj, JsonBuilder ctx) {
        if (obj == null)
            return null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", obj.getExternalId());
        jsonObject.addProperty("remoteId", obj.getId());
        jsonObject.addProperty(SynchableObject.DSC_URL, obj.getHtmlUrl());
        jsonObject.add(SynchableObject.DSC_LAST_SYNC_TIME, ctx.view(obj.getLastSynchTime()));
        jsonObject.addProperty("className", obj.getClass().getName());
        return jsonObject;

    }

}
