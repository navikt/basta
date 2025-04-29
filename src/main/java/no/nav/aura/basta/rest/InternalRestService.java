package no.nav.aura.basta.rest;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.LookupResponse;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.springframework.stereotype.Component;


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
