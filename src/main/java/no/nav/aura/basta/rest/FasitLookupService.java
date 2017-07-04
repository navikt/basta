package no.nav.aura.basta.rest;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.cache.Cache;
import org.jboss.resteasy.spi.BadRequestException;

import com.google.gson.Gson;

import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.envconfig.client.*;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.rest.ResourceElement;

/**
 * Mockable proxy for fasit lookups
 */
@Cache(sMaxAge = 3600)
@Path("/")
public class FasitLookupService {

    private FasitRestClient fasit;
    private Gson gson;

    public FasitLookupService(FasitRestClient fasit) {
        this.fasit = fasit;
        gson = new Gson();
    }

    @GET
    @Path("v1/fasit/applications")
    @Produces(MediaType.APPLICATION_JSON)
    public String getApplications() {
        ApplicationDO[] applications = fasit.get(fasit.getBaseUrl().path("applications").build(), ApplicationDO[].class);
        return gson.toJson(applications);
    }

    @GET
    @Path("/v1/fasit/environments")
    @Produces(MediaType.APPLICATION_JSON)
    public String getEnvironments() {
        return gson.toJson(fasit.getEnvironments());
    }

    @GET
    @Path("/v1/fasit/clusters")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getClusters(@QueryParam("environment") String environment) {
        String environmentsApi = getSystemPropertyOrThrow("fasit:environments_v2.url", "No fasit environments api present");
        Optional<List> list = new RestClient().get(environmentsApi + "/" + environment + "/clusters", List.class);

        if (!list.isPresent()) {
            throw new RuntimeException("Unable to fetch clusters");
        }

        List<Map> clusters = list.get();

        Set<String> clusterNames = clusters.stream().map(c -> (String) c.get("clustername")).collect(Collectors.toSet());
        return clusterNames;
    }

    @GET
    @Path("/v1/fasit/applicationgroups")
    @Produces(MediaType.APPLICATION_JSON)
    public String getApplicationGroups() {
        ApplicationGroupDO[] applicationGroups = fasit.get(fasit.getBaseUrl().path("applicationGroups").build(), ApplicationGroupDO[].class);
        return gson.toJson(applicationGroups);
    }

    @GET
    @Path("/v1/fasit/resources")
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

    private static String getSystemPropertyOrThrow(String key, String message) {
        String property = System.getProperty(key);

        if (property == null) {
            throw new IllegalStateException(message);
        }
        return property;
    }

}
