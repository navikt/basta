package no.nav.aura.basta.rest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import no.nav.aura.envconfig.client.ApplicationGroupDO;
import no.nav.aura.envconfig.client.ApplicationInstanceDO;
import no.nav.aura.envconfig.client.EnvironmentDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;

@Component
@Path("/v1/fasit")
public class FasitRestService {

    private static final Logger logger = LoggerFactory.getLogger(FasitRestService.class);


    @Inject
    private FasitRestClient fasit;

   
    @GET
    @Path("applications")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getApplications() {
        ApplicationInstanceDO[] applications = fasit.get(fasit.getBaseUrl().path("applications").build(),ApplicationInstanceDO[].class);
        return Arrays.stream(applications)
                .map(application -> application.getName())
                .sorted((app1, app2) -> app1.compareToIgnoreCase(app2))
                .collect(Collectors.toList());
    }
    
    @GET
    @Path("environments")
    @Produces(MediaType.APPLICATION_JSON)
    public List<EnvironmentDO> getEnvironments() {
        EnvironmentDO[] environments = fasit.get(fasit.getBaseUrl().path("environments").build(),EnvironmentDO[].class);
        return Arrays.asList(environments);
    }
    
    @GET
    @Path("applicationsgroups")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getApplicationGroups() {
        ApplicationGroupDO[] applicationGroups = fasit.get(fasit.getBaseUrl().path("applicationGroups").build(),ApplicationGroupDO[].class);
        return Arrays.stream(applicationGroups)
                .map(application -> application.getName())
                .sorted((app1, app2) -> app1.compareToIgnoreCase(app2))
                .collect(Collectors.toList());
    }
    
    @GET
    @Path("resources")
    @Produces(MediaType.APPLICATION_JSON)
    public List<EnvironmentDO> getResources(@QueryParam("envClass") String envClass, @QueryParam("envName") String envName, @QueryParam("domain") String domain,
            @QueryParam("app") String application, @QueryParam("type") ResourceTypeDO type, @QueryParam("alias") String alias, @QueryParam("bestmatch") Boolean bestmatch,
            @QueryParam("usage") @DefaultValue("false") Boolean usage) {
        EnvironmentDO[] environments = fasit.get(fasit.getBaseUrl().path("environments").build(),EnvironmentDO[].class);
        return Arrays.asList(environments);
    }
    
    

   
}
