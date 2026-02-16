package no.nav.aura.basta.rest.mq;

import no.nav.aura.basta.backend.mq.MqQueue;
import no.nav.aura.basta.backend.mq.MqQueueManager;
import no.nav.aura.basta.backend.mq.MqService;
import no.nav.aura.basta.domain.input.mq.MQObjectType;
import no.nav.aura.basta.domain.input.mq.MqOrderInput;
import no.nav.aura.basta.util.ValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RestController
@RequestMapping("/rest/v1/mq")
@Transactional
public class MqRestService {

    private static final Logger logger = LoggerFactory.getLogger(MqRestService.class);

    @Inject
    private MqService mq;

    private static final List<String> standardChannelNames = Arrays.asList("CLIENT.MQMON" , "HERMES.SVRCONN" , "MQEXPLORER.SVRCONN" , "MQMON.HTTP" , 
            "RFHUTIL.SVRCONN" , "SRVAURA.ADMIN" );

    @GetMapping("/clusters")
    public ResponseEntity<Collection<String>> getClusters(
            @RequestParam String environmentClass,
            @RequestParam String queueManager) {
        Map<String, String> request = new HashMap<>();
        request.put(MqOrderInput.ENVIRONMENT_CLASS, environmentClass);
        request.put(MqOrderInput.QUEUE_MANAGER, queueManager);
        
        MqQueueManager queueMgr = createQueueManager(request);
        return ResponseEntity.ok(mq.getClusterNames(queueMgr));
    }

    @GetMapping("/queuenames")
    public ResponseEntity<Collection<String>> getQueues(
            @RequestParam String environmentClass,
            @RequestParam String queueManager) {
        Map<String, String> request = new HashMap<>();
        request.put(MqOrderInput.ENVIRONMENT_CLASS, environmentClass);
        request.put(MqOrderInput.QUEUE_MANAGER, queueManager);
        
        MqQueueManager queueMgr = createQueueManager(request);
        return ResponseEntity.ok(mq.findQueuesAliases(queueMgr, "*"));
    }

    @GetMapping("/channels")
    public ResponseEntity<Collection<String>> getChannels(
            @RequestParam(required = false) String channelName,
            @RequestParam String environmentClass,
            @RequestParam String queueManager) {
        Map<String, String> request = new HashMap<>();
        request.put(MqOrderInput.ENVIRONMENT_CLASS, environmentClass);
        request.put(MqOrderInput.QUEUE_MANAGER, queueManager);
        
        MqQueueManager queueMgr = createQueueManager(request);

        Collection<String> channels = mq.findChannelNames(queueMgr, Optional.ofNullable(channelName).orElse("*"));
        
        return ResponseEntity.ok(channels.stream()
                .filter(channel -> !isStandardChannel(channel))
                .collect(Collectors.toList()));
    }

    @GetMapping("/queue")
    public ResponseEntity<?> getQueue(
            @RequestParam String queueName,
            @RequestParam String environmentClass,
            @RequestParam String queueManager) {
        Map<String, String> request = new HashMap<>();
        request.put(MqOrderInput.ENVIRONMENT_CLASS, environmentClass);
        request.put(MqOrderInput.QUEUE_MANAGER, queueManager);
        
        MqQueueManager queueMgr = createQueueManager(request);
        Optional<MqQueue> queue = mq.getQueue(queueMgr, queueName);
        
        if (queue.isPresent()) {
            return ResponseEntity.ok(queue.get());
        }
        logger.info("Queue with name {} not found in {}", queueName, queueMgr);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("queue with name " + queueName + " not found in qm : " + queueMgr.getMqManagerName());
    }

    private boolean isStandardChannel(String channel) {
        if(channel.startsWith("SYSTEM.")){
            return true;
        }
        return standardChannelNames.contains(channel);
    }

    private MqQueueManager createQueueManager(Map<String, String> request) {
        ValidationHelper.validateRequiredParams(request, MqOrderInput.ENVIRONMENT_CLASS, MqOrderInput.QUEUE_MANAGER);

        MqOrderInput input = new MqOrderInput(request, MQObjectType.Queue);
        MqQueueManager queueManager = new MqQueueManager(input.getQueueManagerUri(), input.getEnvironmentClass(), mq.getCredentialMap());
        return queueManager;
    }
}
