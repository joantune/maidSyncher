/**
 * 
 */
package pt.ist.syncherWebRestserver.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import pt.ist.bennu.core.rest.BennuRestResource;
import pt.ist.maidSyncher.api.activeCollab.ACContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 22 de Ago de 2013
 *
 * 
 */
@Path("activeCollab")
public class ActiveCollabResource extends BennuRestResource {
    private static final Gson GSON;
    static {
        GSON = new GsonBuilder().setPrettyPrinting().create();
    }

    @GET
    @Path("instance")
    @Produces(MediaType.APPLICATION_JSON)
    public String getACInstance() {
        JsonObject acInstanceObject = new JsonObject();
        acInstanceObject.addProperty("id", "instance");
        acInstanceObject.addProperty("url", ACContext.getInstance().getServerBaseUrl());
        return GSON.toJson(acInstanceObject);

    }

}
