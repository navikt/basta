package no.nav.aura.basta.rest.vm;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.domain.input.vm.Converters;

import org.jboss.resteasy.spi.BadRequestException;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

@Component
@Path("/vm/domains")
public class DomainsRestService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getDomains(@QueryParam("zone") Zone zone, @QueryParam("envClass") EnvironmentClass environmentClass) {
        if (zone == null || environmentClass == null) {
            throw new BadRequestException("Expected query parameter zone and envClass");
        }
		Map<String, String> domain = new HashMap<>();
        domain.put("domain", Converters.domainFqdnFrom(environmentClass, zone));
        return domain;
    }

    @GET
    @Path("/multisite")
    @Produces(MediaType.APPLICATION_JSON)
	public Map<String, Boolean> isMultisite(@QueryParam("envClass") EnvironmentClass environmentClass, @QueryParam("envName") String environmentName) {
        if (environmentClass == null || environmentName == null) {
            throw new BadRequestException("Expected query parameter envClass ( was  " + environmentClass + ") and envName (was " + environmentName + ")");
        }
		Map<String, Boolean> multisite = Maps.newHashMap();
        multisite.put("multisite", Converters.isMultisite(environmentClass, environmentName));
        return multisite;
    }

}
