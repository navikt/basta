package no.nav.aura.basta.rest;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/internal")
public class InternalRestService {


    @GET
    @Path("isAlive")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response isAlive() {
        return Response.ok().build();
    }

}
