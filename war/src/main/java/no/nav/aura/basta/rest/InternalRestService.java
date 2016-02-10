package no.nav.aura.basta.rest;

import no.nav.aura.basta.domain.SystemNotification;
import no.nav.aura.basta.repository.SystemNotificationRepository;
import no.nav.aura.basta.rest.dataobjects.SystemNotificationDO;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Path("/internal")
public class InternalRestService {


    @GET
    @Path("isAlive")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response isAlive() {
        return Response.ok("application: UP").build();
    }

}
