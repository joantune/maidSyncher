package pt.ist.syncherWebRestserver.json;

import pt.ist.bennu.core.annotation.DefaultJsonAdapter;
import pt.ist.bennu.core.rest.json.DomainObjectViewer;
import pt.ist.bennu.json.JsonBuilder;
import pt.ist.bennu.json.JsonViewer;
import pt.ist.maidSyncher.domain.sync.logs.SyncLog;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@DefaultJsonAdapter(SyncLog.class)
public class SyncLogViewer implements JsonViewer<SyncLog> {


    @Override
    public JsonElement view(SyncLog obj, JsonBuilder ctx) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("id", obj.getExternalId());

        jsonObject.add("SyncStartTime", ctx.view(obj.getSyncStartTime()));

        jsonObject.addProperty("NumberSyncEventsToProcessAtStart", obj.getNumberSyncEventsToProcessAtStart());

        jsonObject.addProperty("NumberGHRequestsAtStartSync", obj.getNumberGHRequestsAtStartSync());
        jsonObject.addProperty("NumberGHRequestsAtEndSync", obj.getNumberGHRequestsAtEndSync());
        jsonObject.addProperty("NumberGHRequestsAtEndActions", obj.getNumberGHRequestsAtEndActions());

        jsonObject.add("SyncGHStartTime", ctx.view(obj.getSyncGHStartTime()));
        jsonObject.add("SyncGHEndTime", ctx.view(obj.getSyncGHEndTime()));

        jsonObject.add("SyncACStartTime", ctx.view(obj.getSyncACStartTime()));
        jsonObject.add("SyncACEndTime", ctx.view(obj.getSyncACEndTime()));

        jsonObject.add("SyncActionsStartTime", ctx.view(obj.getSyncActionsStartTime()));
        jsonObject.add("SyncActionsEndTime", ctx.view(obj.getSyncActionsEndTime()));

        jsonObject.add("SyncEndTime", ctx.view(obj.getSyncEndTime()));

        jsonObject.addProperty("SerializedStackTrace", obj.getSerializedStackTrace());

        jsonObject.addProperty("NrGeneratedSyncActions", obj.getNrGeneratedSyncActions());

        jsonObject.addProperty("NrGeneratedSyncActionsFromRemainingSyncEvents",
                obj.getNrGeneratedSyncActionsFromRemainingSyncEvents());

        jsonObject.addProperty("Status", obj.getStatus());

        jsonObject.addProperty("nrSyncEventConflictLogs", obj.getSyncConflictLogsSet().size());
        jsonObject.addProperty("nrWarnings", obj.getSyncWarningsSet().size());

        jsonObject.add("actions", ctx.view(obj.getSyncActionLogsSet(), DomainObjectViewer.class));

        return jsonObject;
    }

}
