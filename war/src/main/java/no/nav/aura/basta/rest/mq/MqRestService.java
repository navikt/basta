package no.nav.aura.basta.rest.mq;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import no.nav.aura.basta.backend.mq.MqQueue;
import no.nav.aura.basta.backend.mq.MqQueueManager;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.mq.MQObjectType;
import no.nav.aura.basta.domain.input.mq.MqOrderInput;
import no.nav.aura.basta.util.ValidationHelper;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;

@Component
@Path("/v1/mq")
@Transactional
public class MqRestService {

    private static final Logger logger = LoggerFactory.getLogger(MqRestService.class);

    @Inject
    private FasitRestClient fasit;

    @Inject
    private MqService mq;
    
    @GET
    @Path("fasitresources")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ResourceElement> findFasitResourcesWithQueueName(@QueryParam("environmentClass") EnvironmentClass environmentClass, @QueryParam("queueName") String queueName) {
        Collection<ResourceElement> resources = fasit.findResources(EnvClass.valueOf(environmentClass.name()), null, null, null, ResourceTypeDO.Queue, null);
        
        return resources.stream()
                .filter(resource -> resource.getPropertyString("queueName").equals(queueName))
                .collect(Collectors.toList());
    }

   

    @GET
    @Path("clusters")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> getClusters(@Context UriInfo uriInfo) {
        MqQueueManager queueManager = createQueueManager(uriInfo);
        return mq.getClusterNames(queueManager);
    }
    

    @GET
    @Path("queuenames")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> getQueues(@Context UriInfo uriInfo ) {
        MqQueueManager queueManager = createQueueManager(uriInfo);
        return mq.findQueues(queueManager, "*");
    }
    
    @GET
    @Path("queue")
    @Produces(MediaType.APPLICATION_JSON)
    public MqQueue getQueue(@QueryParam("queueName") String queueName, @Context UriInfo uriInfo ) {
        MqQueueManager queueManager = createQueueManager(uriInfo);
        return mq.getQueue(queueManager, queueName);
    }
    
    
    
    private MqQueueManager createQueueManager(UriInfo uriInfo) {
        Map<String, String> request = extractQueryParams(uriInfo);
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.QUEUE_MANAGER);

        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass());
        return queueManager;
    }

    private Map<String, String> extractQueryParams(UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        HashMap<String, String> request = new HashMap<>();
        for (String key : queryParameters.keySet()) {
            request.put(key, queryParameters.getFirst(key));
        }
        return request;
    }

 

}
