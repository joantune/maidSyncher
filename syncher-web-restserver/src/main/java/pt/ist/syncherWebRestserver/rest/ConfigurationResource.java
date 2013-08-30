/**
 * 
 */
package pt.ist.syncherWebRestserver.rest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import pt.ist.Main;
import pt.ist.bennu.core.rest.BennuRestResource;
import pt.ist.bennu.core.rest.json.UserSessionViewer;
import pt.ist.bennu.core.security.Authenticate;
import pt.ist.bennu.core.security.UserSession;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.maidSyncher.domain.MaidRoot;
import pt.ist.syncherWebRestserver.SyncherSystem;
import pt.utl.ist.fenix.tools.util.Strings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 29 de Ago de 2013
 * 
 *         Endpoint used to retrieve general data from the app's configuration
 */
@Path("configuration")
public class ConfigurationResource extends BennuRestResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getData() {
        final JsonObject data = new JsonObject();
        merge(data, getBuilder().view(getCasConfigContext()).getAsJsonObject());
        merge(data, getBuilder().view(Authenticate.getUserSession(), UserSession.class, UserSessionViewer.class)
                .getAsJsonObject());
        data.addProperty("syncherRunning", MaidRoot.getInstance().getRunScheduler());
        data.add("repositoriesToIgnore", getBuilder().view(MaidRoot.getInstance().getRepositoriesToIgnore()));
//        JsonElement repsToIgnore = getBuilder().view(MaidRoot.getInstance().getRepositoriesToIgnore());
//        JsonObject repsToIgnoreObject = (JsonObject) (repsToIgnore == null ? JsonNull.INSTANCE : repsToIgnore.getAsJsonObject());

        data.addProperty("schedule", SyncherSystem.getSchedule());

        return toJson(data);
    }

    @POST
    @Path("syncherTask")
    @Produces(MediaType.APPLICATION_JSON)
    @Atomic(mode = TxMode.WRITE)
    public String syncherTask(@FormParam("enable") boolean enable) throws IOException {
        accessControl("#managers");
        MaidRoot.getInstance().setRunScheduler(enable);
        SyncherSystem.initSchedule();

        return getData();
    }

    @GET
    @Path("ghRepositories")
    @Produces(MediaType.APPLICATION_JSON)
    public String getRepositories() throws IOException {
        accessControl("#managers"); //let's prevent DoS by exhausting the number of requests to GH
        final JsonObject object = new JsonObject();
        return view(Main.getGHRepositories());
    }

    @POST
    @Path("ghRepositoriesToIgnore")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Atomic(mode = TxMode.WRITE)
    public String setIgnoredRepositories(String ignoredRepositories) {
        accessControl("#managers");
        Set<String> repositoriesToBeIgnored = new HashSet<>();
        JsonElement element = new JsonParser().parse(ignoredRepositories);
        for (JsonElement repoName : element.getAsJsonArray()) {
            repositoriesToBeIgnored.add(repoName.getAsString());
        }
        MaidRoot.getInstance().setRepositoriesToIgnore(new Strings(repositoriesToBeIgnored));

        return view(MaidRoot.getInstance().getRepositoriesToIgnore());
    }

}
