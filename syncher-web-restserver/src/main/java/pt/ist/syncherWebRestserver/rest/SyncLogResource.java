package pt.ist.syncherWebRestserver.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import pt.ist.bennu.core.rest.BennuRestResource;

@Path("test")
public class SyncLogResource extends BennuRestResource {

    public static int counter = 0;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String view() {
        return String.valueOf(counter);
//        return "espectacular";
    }


}
