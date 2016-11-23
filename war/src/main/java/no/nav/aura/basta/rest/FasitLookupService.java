package no.nav.aura.basta.rest;

import java.net.URI;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.cache.Cache;
import org.jboss.resteasy.spi.BadRequestException;

import com.google.gson.Gson;

import no.nav.aura.envconfig.client.ApplicationDO;
import no.nav.aura.envconfig.client.ApplicationGroupDO;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;

/**
 * Mockable proxy for fasit lookups
 *
 */
@Cache(sMaxAge = 3600)
@Path("/v1/fasit")
public class FasitLookupService {

    // private static final Logger logger = LoggerFactory.getLogger(FasitLookupService.class);
    private FasitRestClient fasit;
    private Gson gson;

    public FasitLookupService(FasitRestClient fasit) {
        this.fasit = fasit;
        gson = new Gson();
    }

    @GET
    @Path("applications")
    @Produces(MediaType.APPLICATION_JSON)
    public String getApplications() {
        ApplicationDO[] applications = fasit.get(fasit.getBaseUrl().path("applications").build(), ApplicationDO[].class);
        return gson.toJson(applications);
        // return applications;
    }

    @GET
    @Path("environments")
    @Produces(MediaType.APPLICATION_JSON)
    public String getEnvironments() {
        return gson.toJson(fasit.getEnvironments());
    }

    @GET
    @Path("applicationgroups")
    @Produces(MediaType.APPLICATION_JSON)
    public String getApplicationGroups() {
        ApplicationGroupDO[] applicationGroups = fasit.get(fasit.getBaseUrl().path("applicationGroups").build(), ApplicationGroupDO[].class);
        return gson.toJson(applicationGroups);
    }

    @GET
    @Path("resources")
    @Produces(MediaType.APPLICATION_JSON)
    public String getResources(@QueryParam("envClass") String environmentClass, @QueryParam("environment") String environment, @QueryParam("application") String application,
            @QueryParam("type") ResourceTypeDO type, @QueryParam("alias") String alias, @QueryParam("bestmatch") Boolean bestmatch, @QueryParam("usage") @DefaultValue("false") Boolean usage) {
        DomainDO domain = null;
        EnvClass envClass = (environmentClass != null) ? EnvClass.valueOf(environmentClass) : null;
        if (envClass == null && environment == null) {
            throw new BadRequestException("Missing parameter! envClass and/or environment is required");
        }

        URI url = fasit.buildResourceQuery(envClass, environment, domain, application, type, alias, bestmatch, usage);
        ResourceElement[] resources = fasit.get(url, ResourceElement[].class);
        return gson.toJson(resources);

    }

}
