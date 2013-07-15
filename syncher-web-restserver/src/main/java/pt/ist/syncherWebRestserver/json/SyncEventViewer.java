/**
 * 
 */
package pt.ist.syncherWebRestserver.json;

import pt.ist.bennu.core.annotation.DefaultJsonAdapter;
import pt.ist.bennu.json.JsonBuilder;
import pt.ist.bennu.json.JsonViewer;
import pt.ist.maidSyncher.domain.sync.SyncEvent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 12 de Jul de 2013
 *
 * 
 */
@DefaultJsonAdapter(SyncEvent.class)
public class SyncEventViewer implements JsonViewer<SyncEvent> {

    /* (non-Javadoc)
     * @see pt.ist.bennu.json.JsonViewer#view(java.lang.Object, pt.ist.bennu.json.JsonBuilder)
     */
    @Override
    public JsonElement view(SyncEvent syncEvent, JsonBuilder ctx) {
        if (syncEvent == null)
            return null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", syncEvent.getExternalId());
        jsonObject.addProperty("apiObjectClassName", syncEvent.getApiObjectClassName());
        jsonObject.add("changedPropertyDescriptorNames", ctx.view(syncEvent.getChangedPropertyDescriptorNames()));
        jsonObject.add("dateOfChange", ctx.view(syncEvent.getDateOfChange()));
        jsonObject.add("dsiElement", ctx.view(syncEvent.getDsiElement()));
        jsonObject.add("originObject", ctx.view(syncEvent.getOriginObject()));
        jsonObject.add("targetSyncUniverse", ctx.view(syncEvent.getTargetSyncUniverse()));
        jsonObject.add("typeOfChangeEvent", ctx.view(syncEvent.getTypeOfChangeEvent()));
        return jsonObject;
    }

}
