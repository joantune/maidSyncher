package pt.ist.syncherWebRestserver.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import pt.ist.maidSyncher.domain.MaidRoot;

@Path("test")
public class SyncLogResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String view() {
        return MaidRoot.getInstance().getGhRepositoriesSet().iterator().next().getName();
//        return "espectacular";
    }

}
