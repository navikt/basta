package no.nav.aura.basta.rest.mq;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import no.nav.aura.basta.backend.mq.MqQueue;
import no.nav.aura.basta.backend.mq.MqQueueManager;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.domain.input.mq.MQObjectType;
import no.nav.aura.basta.domain.input.mq.MqOrderInput;
import no.nav.aura.basta.util.ValidationHelper;

@Component
@Path("/v1/mq")
@Transactional
public class MqRestService {

    private static final Logger logger = LoggerFactory.getLogger(MqRestService.class);

    @Inject
    private MqService mq;

    private static final List<String> standardChannelNames = Arrays.asList("CLIENT.MQMON" , "HERMES.SVRCONN" , "MQEXPLORER.SVRCONN" , "MQMON.HTTP" , 
            "RFHUTIL.SVRCONN" , "SRVAURA.ADMIN" );

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
    public Collection<String> getQueues(@Context UriInfo uriInfo) {
        MqQueueManager queueManager = createQueueManager(uriInfo);
        return mq.findQueuesAliases(queueManager, "*");
    }

    @GET
    @Path("channels")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> getChannels(@QueryParam("channelName") String channelName, @Context UriInfo uriInfo) {
        MqQueueManager queueManager = createQueueManager(uriInfo);

        Collection<String> channels = mq.findChannelNames(queueManager, Optional.ofNullable(channelName).orElse("*"));
        
        return channels.stream()
                .filter(channel -> !isStandardChannel(channel))
                .collect(Collectors.toList());
    }

    private boolean isStandardChannel(String channel) {
        if(channel.startsWith("SYSTEM.")){
            return true;
        }
        return standardChannelNames.contains(channel);
    }

    @GET
    @Path("queue")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQueue(@QueryParam("queueName") String queueName, @Context UriInfo uriInfo) {
        MqQueueManager queueManager = createQueueManager(uriInfo);
        Optional<MqQueue> queue = mq.getQueue(queueManager, queueName);
        if (queue.isPresent()) {
            return Response.ok(queue.get()).build();
        }
        logger.info("Queue with name {} not found in {}", queueName, queueManager);
        return Response.status(Status.NOT_FOUND).entity("queue with name " + queueName + " not found in qm : " + queueManager.getMqManagerName()).build();

    }

    private MqQueueManager createQueueManager(UriInfo uriInfo) {
        Map<String, String> request = extractQueryParams(uriInfo);
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.QUEUE_MANAGER);

        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass(), mq.getCredentialMap());
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
