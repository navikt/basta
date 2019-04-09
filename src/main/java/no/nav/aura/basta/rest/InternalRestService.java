package no.nav.aura.basta.rest;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.LookupResponse;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/internal")
public class InternalRestService {
    @Inject
    private Vault vault;

    @GET
    @Path("/isAlive")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response isAlive() {
        return Response.ok().build();
    }

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response health() throws VaultException {
        LookupResponse self = vault.auth().lookupSelf();
        return Response.ok(self.getTTL()).build();
    }
}
