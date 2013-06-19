package pt.ist.syncherWebRestserver.rest;

import java.util.Collection;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import pt.ist.bennu.core.rest.DomainObjectResource;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.maidSyncher.domain.sync.logs.SyncLog;

@Path("synclogs")
public class SyncLogResource extends DomainObjectResource<SyncLog> {

    private static final String REMAINING_EVENTS = "remainingevents";
    private static final String WARNINGS = "warnings";
    private static final String CONFLICTS = "conflicts";
    private static final String ACTIONS = "actions";
    public static int counter = 0;

    @Override
    @PUT
    @Path("{oid}")
    public String update(String oid, String jsonData) {
        throw new WebApplicationException(Status.NOT_FOUND);
    }

    @Override
    @POST
    @Produces("application/json")
    public String create(String jsonData) {
        throw new WebApplicationException(Status.NOT_FOUND);
    }

    @Override
    @DELETE
    @Path("{oid}")
    @Produces("application/json")
    public String delete(String oid) {
        throw new WebApplicationException(Status.NOT_FOUND);
    }


    @GET
    @Path("{oid}/" + ACTIONS)
    @Produces(MediaType.APPLICATION_JSON)
    public String viewSyncActionLogs(@PathParam("oid") String oid) {
        accessControl(getAccessExpression());
        return view(((SyncLog) readDomainObject(oid)).getSyncActionLogsSet(), ACTIONS);
    }

    @GET
    @Path(REMAINING_EVENTS)
    @Produces(MediaType.APPLICATION_JSON)
    public String viewRemainingSyncEventsLogs(@PathParam("oid") String oid) {
        accessControl(getAccessExpression());
        return view(MaidRoot.getInstance().getSyncEventsToProcessSet(), REMAINING_EVENTS);
    }

    @GET
    @Path("{oid}/" + CONFLICTS)
    @Produces(MediaType.APPLICATION_JSON)
    public String viewSyncConflictLogs(@PathParam("oid") String oid) {
        accessControl(getAccessExpression());
        return view(((SyncLog) readDomainObject(oid)).getSyncConflictLogsSet(), CONFLICTS);
    }

    @GET
    @Path("{oid}/" + WARNINGS)
    @Produces(MediaType.APPLICATION_JSON)
    public String viewSyncWarningLogs(@PathParam("oid") String oid) {
        accessControl(getAccessExpression());
        return view(((SyncLog) readDomainObject(oid)).getSyncWarningsSet(), WARNINGS);
    }

    @Override
    public Collection<SyncLog> all() {
        return MaidRoot.getInstance().getSyncLogsSet();
    }

    @Override
    public String collectionKey() {
        return "synclogs";
    }

    @Override
    public boolean delete(SyncLog arg0) {
        throw new UnsupportedOperationException("Nooo nooo no delete for you");
    }

    @Override
    public String getAccessExpression() {
        return "anyone";
    }

    @Override
    public Class<SyncLog> type() {
        return SyncLog.class;
    }


}
