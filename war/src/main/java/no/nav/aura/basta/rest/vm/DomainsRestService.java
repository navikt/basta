package no.nav.aura.basta.rest.vm;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.Maps;

import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.domain.input.vm.Converters;
import no.nav.aura.basta.domain.input.vm.EnvironmentClass;

import org.jboss.resteasy.spi.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Path("/vm/domains")
public class DomainsRestService {

    @GET
    public String getDomains(@QueryParam("zone") Zone zone, @QueryParam("envClass") EnvironmentClass environmentClass) {
        if (zone == null || environmentClass == null) {
            throw new BadRequestException("Expected query parameter zone and envClass");
        }
        return Converters.domainFqdnFrom(environmentClass, zone);
    }

    @GET
    @Path("/multisite")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, Boolean> isMultisite(@QueryParam("envClass") EnvironmentClass environmentClass, @QueryParam("envName") String environmentName) {
        if (environmentClass == null || environmentName== null) {
            throw new BadRequestException("Expected query parameter envClass ( was  " + environmentClass + ") and envName (was " + environmentName + ")");
        }
        HashMap<String,Boolean> multisite = Maps.newHashMap();
        multisite.put("multisite", Converters.isMultisite(environmentClass, environmentName));
        return multisite;
    }

}
