package no.nav.aura.basta.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import no.nav.aura.basta.Converters;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.Zone;

import org.jboss.resteasy.spi.BadRequestException;
import org.springframework.stereotype.Component;

@Component
@Path("/domains")
public class DomainsRestService {

    @GET
    public String getDomains(@QueryParam("zone") Zone zone, @QueryParam("envClass") EnvironmentClass environmentClass) {
        if (zone == null || environmentClass == null) {
            throw new BadRequestException("Expected query parameter zone and envClass");
        }
        return Converters.domainFqdnFrom(environmentClass, zone);
    }

}
