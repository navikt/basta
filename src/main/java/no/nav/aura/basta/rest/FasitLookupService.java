package no.nav.aura.basta.rest;

import com.google.gson.Gson;

import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.fasit.deprecated.FasitRestClient;
import no.nav.aura.basta.backend.fasit.deprecated.ResourceElement;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.ApplicationDO;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.ApplicationGroupDO;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.DomainDO;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.ResourceTypeDO;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ApplicationPayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourceType;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.ScopePayload;
import no.nav.aura.basta.backend.fasit.deprecated.payload.Zone;

import org.jboss.resteasy.annotations.cache.Cache;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mockable proxy for fasit lookups
 */
@Cache(sMaxAge = 3600)
@Path("/")
public class FasitLookupService {

    private FasitRestClient fasit;
    private RestClient fasitClient;
    private Gson gson;

    public FasitLookupService() {}

    public FasitLookupService(FasitRestClient fasit, RestClient restClient) {
        this.fasit = fasit;
        this.fasitClient = restClient;
        gson = new Gson();
    }

    @GET
    @Path("v2/fasit/applications")
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
        String environmentsApi = getSystemPropertyOrThrow("fasit_environments_v2_url", "No fasit environments api " +
                "present");
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
        DomainDO.EnvClass envClass = (environmentClass != null) ? DomainDO.EnvClass.valueOf(environmentClass) : null;
        if (envClass == null && environment == null) {
            throw new org.jboss.resteasy.spi.BadRequestException("Missing parameter! envClass and/or environment is required");
        }

        URI url = fasit.buildResourceQuery(envClass, environment, domain, application, type, alias, bestmatch, usage);
        ResourceElement[] resources = fasit.get(url, ResourceElement[].class);
        return gson.toJson(resources);

    }

    @GET
    @Path("/v2/fasit/resources")
    @Produces(MediaType.APPLICATION_JSON)
public String findResources(@QueryParam("environmentclass") String environmentClass, @QueryParam("environment") String environment, @QueryParam("application") String application, @QueryParam("type") ResourceType type, @QueryParam("alias") String alias, @QueryParam("zone")Zone zone) {
        if (environmentClass == null) {
            throw new javax.ws.rs.BadRequestException("Missing required parameter environmentClass");
        }

        final ScopePayload scope = new ScopePayload(environmentClass)
                .environment(environment)
                .application(application)
                .zone(zone);

        ResourcesListPayload fasitResources = fasitClient.findFasitResources(type, alias, scope);

        return gson.toJson(fasitResources.getResources());
    }

    private static String getSystemPropertyOrThrow(String key, String message) {
        String property = System.getProperty(key);

        if (property == null) {
            throw new IllegalStateException(message);
        }
        return property;
    }

}
