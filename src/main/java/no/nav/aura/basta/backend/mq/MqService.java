package no.nav.aura.basta.backend.mq;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.PCFException;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFParameter;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MqService {
    private Logger log = LoggerFactory.getLogger(MqService.class);

    private Map<EnvironmentClass, MqAdminUser> credentialMap = new HashMap<>();

    public MqService(Map<EnvironmentClass, MqAdminUser> credentialMap) {
        this.credentialMap = credentialMap;
    }

    public void createQueue(MqQueueManager queueManager, MqQueue queue) {
        if (queueExists(queueManager, queue.getName())) {
            throw new IllegalArgumentException("Queue " + queue.getName() + " already exists");
        }
        PCFMessage createQueuerequest = new PCFMessage(MQConstants.MQCMD_CREATE_Q);
        createQueuerequest.addParameter(MQConstants.MQCA_Q_NAME, queue.getName());
        createQueuerequest.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_LOCAL);
        createQueuerequest.addParameter(MQConstants.MQCA_Q_DESC, queue.getDescription());
        createQueuerequest.addParameter(MQConstants.MQIA_MAX_Q_DEPTH, queue.getMaxDepth());
        createQueuerequest.addParameter(MQConstants.MQIA_MAX_MSG_LENGTH, queue.getMaxSizeInBytes());
        if (queue.shouldCreateBoq()) {
            createQueuerequest.addParameter(MQConstants.MQCA_BACKOUT_REQ_Q_NAME, queue.getBackoutQueueName());
            createQueuerequest.addParameter(MQConstants.MQIA_BACKOUT_THRESHOLD, queue.getBackoutThreshold());
        }
        execute(queueManager, createQueuerequest);
        log.info("Created queue {}", queue.getName());
    }

    public void createBackoutQueue(MqQueueManager queueManager, MqQueue queue) {
        if (queueExists(queueManager, queue.getBackoutQueueName())) {
            throw new IllegalArgumentException("Backout queue " + queue.getBackoutQueueName() + " already exists");
        }
        PCFMessage createBoqRequest = new PCFMessage(MQConstants.MQCMD_CREATE_Q);
        createBoqRequest.addParameter(MQConstants.MQCA_Q_NAME, queue.getBackoutQueueName());
        createBoqRequest.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_LOCAL);
        createBoqRequest.addParameter(MQConstants.MQCA_Q_DESC, "Backout for " + queue.getName());
        createBoqRequest.addParameter(MQConstants.MQIA_MAX_Q_DEPTH, queue.getMaxDepth());
        createBoqRequest.addParameter(MQConstants.MQIA_MAX_MSG_LENGTH, queue.getMaxSizeInBytes());
        execute(queueManager, createBoqRequest);
        log.info("Created backout queue {}", queue.getBackoutQueueName());
    }

    public void createAlias(MqQueueManager queueManager, MqQueue queue) {
        if (queueExists(queueManager, queue.getAlias())) {
            throw new IllegalArgumentException("Alias " + queue.getAlias() + " already exists");
        }
        PCFMessage createAliasrequest = new PCFMessage(MQConstants.MQCMD_CREATE_Q);
        createAliasrequest.addParameter(MQConstants.MQCA_Q_NAME, queue.getAlias());
        createAliasrequest.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_ALIAS);
        createAliasrequest.addParameter(MQConstants.MQCA_BASE_OBJECT_NAME, queue.getName());
        if (queue.getClusterName().isPresent()) {
            createAliasrequest.addParameter(MQConstants.MQCA_CLUSTER_NAMELIST, queue.getClusterName().get());
        }

        execute(queueManager, createAliasrequest);
        log.info("Created queue alias: " + queue.getAlias());
    }

    public void createTopic(MqQueueManager queueManager, MqTopic topic) {
        if (queueExists(queueManager, topic.getName())) {
            throw new IllegalArgumentException("Topic " + topic.getName() + " already exists");
        }
        PCFMessage createTopicrequest = new PCFMessage(MQConstants.MQCMD_CREATE_TOPIC);
        createTopicrequest.addParameter(MQConstants.MQCA_TOPIC_NAME, topic.getName());
        createTopicrequest.addParameter(MQConstants.MQCA_TOPIC_STRING, topic.getTopicString());
        createTopicrequest.addParameter(MQConstants.MQCA_TOPIC_DESC, topic.getDescription());

        execute(queueManager, createTopicrequest);
        log.info("Created topic {}", topic.getName());
    }

    private void setQueueAuthorization(MqQueueManager queueManager, MqQueue queue) {
        int[] listRemoveQueueAuth = new int[1];
        listRemoveQueueAuth[0] = MQConstants.MQAUTH_ALL;

        int[] listAddQueueAuth = new int[5];
        listAddQueueAuth[0] = MQConstants.MQAUTH_BROWSE;
        listAddQueueAuth[1] = MQConstants.MQAUTH_DISPLAY;
        listAddQueueAuth[2] = MQConstants.MQAUTH_INQUIRE;
        listAddQueueAuth[3] = MQConstants.MQAUTH_INPUT;
        listAddQueueAuth[4] = MQConstants.MQAUTH_OUTPUT;

        PCFMessage deleteQueueAuthrequest = new PCFMessage(MQConstants.MQCMD_SET_AUTH_REC);
        deleteQueueAuthrequest.addParameter(MQConstants.MQCACF_AUTH_PROFILE_NAME, queue.getAlias());
        deleteQueueAuthrequest.addParameter(MQConstants.MQIACF_OBJECT_TYPE, MQConstants.MQOT_Q);
        deleteQueueAuthrequest.addParameter(MQConstants.MQIACF_AUTH_REMOVE_AUTHS, listRemoveQueueAuth);
        deleteQueueAuthrequest.addParameter(MQConstants.MQCACF_GROUP_ENTITY_NAMES, getGroupList("mqusers"));

        execute(queueManager, deleteQueueAuthrequest);

        PCFMessage setQueueAliasAuthrequest = new PCFMessage(MQConstants.MQCMD_SET_AUTH_REC);
        setQueueAliasAuthrequest.addParameter(MQConstants.MQCACF_AUTH_PROFILE_NAME, queue.getAlias());
        setQueueAliasAuthrequest.addParameter(MQConstants.MQIACF_OBJECT_TYPE, MQConstants.MQOT_Q);
        setQueueAliasAuthrequest.addParameter(MQConstants.MQIACF_AUTH_ADD_AUTHS, listAddQueueAuth);
        setQueueAliasAuthrequest.addParameter(MQConstants.MQCACF_GROUP_ENTITY_NAMES, getGroupList("mqusers"));

        execute(queueManager, setQueueAliasAuthrequest);

        PCFMessage setQueueAuthrequest = new PCFMessage(MQConstants.MQCMD_SET_AUTH_REC);
        setQueueAuthrequest.addParameter(MQConstants.MQCACF_AUTH_PROFILE_NAME, queue.getName());
        setQueueAuthrequest.addParameter(MQConstants.MQIACF_OBJECT_TYPE, MQConstants.MQOT_Q);
        setQueueAuthrequest.addParameter(MQConstants.MQIACF_AUTH_ADD_AUTHS, listAddQueueAuth);
        setQueueAuthrequest.addParameter(MQConstants.MQCACF_GROUP_ENTITY_NAMES, getGroupList("mqusers"));

        execute(queueManager, setQueueAuthrequest);

        log.info("Updated queue authorization for " + queue.getName() + " and " + queue.getAlias());
    }

    /**
     * @return true if queue is deleted, false if it does not exist
     */
    public boolean deleteQueue(MqQueueManager queueManager, String name) {
        if (!queueExists(queueManager, name)) {
            return false;
        }
        log.info("Deleting queue {}", name);
        if (!isQueueEmpty(queueManager, name)) {
            log.error("Queue {} contains unread messages, please clear queue before deleting it.", name);
            return false;
        }
        PCFMessage deleteRequest = new PCFMessage(MQConstants.MQCMD_DELETE_Q);
        deleteRequest.addParameter(MQConstants.MQCA_Q_NAME, name);
        execute(queueManager, deleteRequest);
        return true;
    }

    public void disableQueue(MqQueueManager queueManager, MqQueue queue) {
        PCFMessage disableRequest = new PCFMessage(MQConstants.MQCMD_CHANGE_Q);
        disableRequest.addParameter(MQConstants.MQCA_Q_NAME, queue.getAlias());
        disableRequest.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_ALIAS);
        disableRequest.addParameter(MQConstants.MQIA_INHIBIT_GET, MQConstants.MQQA_GET_INHIBITED);
        disableRequest.addParameter(MQConstants.MQIA_INHIBIT_PUT, MQConstants.MQQA_PUT_INHIBITED);
        execute(queueManager, disableRequest);
        log.info("Disabled queue {}", queue.getAlias());

    }

    public void enableQueue(MqQueueManager queueManager, MqQueue queue) {
        PCFMessage enableRequest = new PCFMessage(MQConstants.MQCMD_CHANGE_Q);
        enableRequest.addParameter(MQConstants.MQCA_Q_NAME, queue.getAlias());
        enableRequest.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_ALIAS);
        enableRequest.addParameter(MQConstants.MQIA_INHIBIT_GET, MQConstants.MQQA_GET_ALLOWED);
        enableRequest.addParameter(MQConstants.MQIA_INHIBIT_PUT, MQConstants.MQQA_PUT_ALLOWED);
        execute(queueManager, enableRequest);
        log.info("Enabled queue {}", queue.getAlias());

    }

    public Optional<MqQueue> getQueue(MqQueueManager queueManager, String name) {
        if (!queueExists(queueManager, name)) {
            return Optional.empty();
        }
        log.debug("getQueue: " + name);
        PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q);
        request.addParameter(MQConstants.MQCA_Q_NAME, name);
        request.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_ALL);

        PCFMessage[] responses = execute(queueManager, request);
        PCFMessage response = responses[0];

        try {
            MqQueue q = new MqQueue();
            if (response.getIntParameterValue(MQConstants.MQIA_Q_TYPE) == MQConstants.MQQT_ALIAS) {
                String alias = response.getStringParameterValue(MQConstants.MQCA_BASE_Q_NAME).trim();
                q = getQueue(queueManager, alias).get();
                q.setAlias(name);
            } else {
                q.setName(name);
                q.setDescription(response.getStringParameterValue(MQConstants.MQCA_Q_DESC).trim());
                if (queueExists(queueManager, "QA." + name)) {
                    q.setAlias("QA." + name);
                }
                q.setBoqName(response.getStringParameterValue(MQConstants.MQCA_BACKOUT_REQ_Q_NAME).trim());
                q.setBackoutThreshold(response.getIntParameterValue(MQConstants.MQIA_BACKOUT_THRESHOLD));
                q.setMaxDepth(response.getIntParameterValue(MQConstants.MQIA_MAX_Q_DEPTH));
                q.setMaxSizeInBytes(response.getIntParameterValue(MQConstants.MQIA_MAX_MSG_LENGTH));
            }
            return Optional.of(q);
        } catch (PCFException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean queueExists(MqQueueManager queueManager, String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return !findQueues(queueManager, name, MQConstants.MQQT_ALL).isEmpty();
    }

    private Collection<String> findQueues(MqQueueManager queueManager, String name, int type) {
        PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q_NAMES);
        request.addParameter(MQConstants.MQCA_Q_NAME, name);
        request.addParameter(MQConstants.MQIA_Q_TYPE, type);

        PCFMessage[] responses = execute(queueManager, request);

        String[] names = (String[]) responses[0].getParameterValue(MQConstants.MQCACF_Q_NAMES);
        return Stream.of(names)
                .map(n -> n.trim())
                .collect(Collectors.toList());
    }

    public Collection<String> findQueuesAliases(MqQueueManager queueManager, String name) {
        return findQueues(queueManager, name, MQConstants.MQQT_ALIAS);
    }

    public void createChannel(MqQueueManager queueManager, MqChannel channel) {

        log.info("Create channel {}", channel.getName());
        PCFMessage createChannelrequest = new PCFMessage(MQConstants.MQCMD_CREATE_CHANNEL);
        createChannelrequest.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
        createChannelrequest.addParameter(MQConstants.MQIACH_CHANNEL_TYPE, channel.getType());
        createChannelrequest.addParameter(MQConstants.MQCACH_DESC, channel.getDescription());
        createChannelrequest.addParameter(MQConstants.MQIACH_MAX_INSTANCES, channel.getMaxInstances());
        createChannelrequest.addParameter(MQConstants.MQIACH_MAX_INSTS_PER_CLIENT, channel.getMaxInstancesPerClient());
        if (channel.isTlsEnabled()) {
            createChannelrequest.addParameter(MQConstants.MQCACH_SSL_CIPHER_SPEC, channel.getCipherSuite());
        }

        execute(queueManager, createChannelrequest);
        log.info("Created channel {}", channel.getName());
        //setChannelAuthorization(queueManager, channel);
    }

    private void setChannelAuthorization(MqQueueManager queueManager, MqChannel channel) {
        PCFMessage setChannelAuthrequest = new PCFMessage(MQConstants.MQCMD_SET_CHLAUTH_REC);
        setChannelAuthrequest.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
        setChannelAuthrequest.addParameter(MQConstants.MQIACF_CHLAUTH_TYPE, MQConstants.MQCAUT_USERMAP);
        setChannelAuthrequest.addParameter(MQConstants.MQIACF_ACTION, MQConstants.MQACT_REPLACE);
        setChannelAuthrequest.addParameter(MQConstants.MQCACH_CONNECTION_NAME, channel.getIpRange());
        setChannelAuthrequest.addParameter(MQConstants.MQCACH_CLIENT_USER_ID, channel.getUserName());
        setChannelAuthrequest.addParameter(MQConstants.MQCACH_MCA_USER_ID, channel.getUserName());
        setChannelAuthrequest.addParameter(MQConstants.MQCA_CHLAUTH_DESC, channel.getDescription());

        execute(queueManager, setChannelAuthrequest);
        log.info("Updated channel and authentication object for {}", channel.getName());
    }

    public void stopChannel(MqQueueManager queueManager, MqChannel channel) {
        log.info("Stopping channel " + channel.getName());
        if (channelExists(queueManager, channel)) {
            try {
                PCFMessage stopChannelrequest = new PCFMessage(MQConstants.MQCMD_STOP_CHANNEL);
                stopChannelrequest.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
                stopChannelrequest.addParameter(MQConstants.MQIACF_MODE, MQConstants.MQMODE_FORCE);
                execute(queueManager, stopChannelrequest);
            } catch (Exception e) {
                log.error("Could not stop channel", e);
            }

        }
    }
    
    public void startChannel(MqQueueManager queueManager, MqChannel channel) {
        log.info("Starting channel " + channel.getName());
        if (channelExists(queueManager, channel)) {
            try {
                PCFMessage stopChannelrequest = new PCFMessage(MQConstants.MQCMD_START_CHANNEL);
                stopChannelrequest.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
                execute(queueManager, stopChannelrequest);
            } catch (Exception e) {
                log.error("Could not start channel", e);
            }

        }
        
    }

    public void deleteChannel(MqQueueManager queueManager, MqChannel channel) {
        if (!channelExists(queueManager, channel)) {
            log.warn("Channel {} does not exist", channel.getName());
            return;
        }
        PCFMessage deleteRequest = new PCFMessage(MQConstants.MQCMD_DELETE_CHANNEL);
        deleteRequest.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
        execute(queueManager, deleteRequest);

        log.info("Deleted channel {}", channel.getName());

        List<MqChannel> channelAutentications = findChannelAutentications(queueManager, channel);
        for (MqChannel mqChannel : channelAutentications) {
            deleteChannelAuthentication(queueManager, mqChannel);
        }
    }

    private void deleteChannelAuthentication(MqQueueManager queueManager, MqChannel channel) {
        PCFMessage deleteChannelAuthrequest = new PCFMessage(MQConstants.MQCMD_SET_CHLAUTH_REC);
        deleteChannelAuthrequest.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
        deleteChannelAuthrequest.addParameter(MQConstants.MQIACF_CHLAUTH_TYPE, MQConstants.MQCAUT_USERMAP);
        deleteChannelAuthrequest.addParameter(MQConstants.MQIACF_ACTION, MQConstants.MQACT_REMOVE);
        deleteChannelAuthrequest.addParameter(MQConstants.MQCACH_CONNECTION_NAME, channel.getIpRange());
        deleteChannelAuthrequest.addParameter(MQConstants.MQCACH_CLIENT_USER_ID, channel.getUserName());

        execute(queueManager, deleteChannelAuthrequest);
        log.info("Deleted channel authentication object {}", channel.getName());
    }

    private List<MqChannel> findChannelAutentications(MqQueueManager queueManager, MqChannel channel) {
        PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_CHLAUTH_RECS);
        request.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
        PCFMessage[] responses = execute(queueManager, request);

        List<MqChannel> channelAuths = new ArrayList<>();

        for (PCFMessage pcfMessage : responses) {
            MqChannel channelAuth = new MqChannel(channel.getName());
            channelAuth.setUserName(pcfMessage.getParameter(MQConstants.MQCACH_CLIENT_USER_ID).getStringValue().trim());
            channelAuth.setIpRange(pcfMessage.getParameter(MQConstants.MQCACH_CONNECTION_NAME).getStringValue().trim());
            channelAuths.add(channelAuth);
        }
        log.info("found {} channel auths for {}", channelAuths.size(), channel.getName());
        return channelAuths;
    }

    private String[] getGroupList(String group) {
        String[] listGroup = new String[1];
        listGroup[0] = group;
        return listGroup;
    }

    private boolean channelExists(MqQueueManager queueManager, MqChannel channel) {
       return !findChannelNames(queueManager, channel.getName()).isEmpty();
    }

    public Collection<String> findChannelNames(MqQueueManager queueManager, String name) {

        log.info("Searching for channel {}", name);
        PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_CHANNEL_NAMES);
        request.addParameter(MQConstants.MQCACH_CHANNEL_NAME, name);
        request.addParameter(MQConstants.MQIACH_CHANNEL_TYPE, MQConstants.MQCHT_SVRCONN);
        PCFMessage[] responses = execute(queueManager, request);
        String[] names = (String[]) responses[0].getParameterValue(MQConstants.MQCACH_CHANNEL_NAMES);
        if (names==null){
            return new ArrayList<>();
        }

        return Arrays.stream(names)
                .map(channelName -> channelName.trim())
                .collect(Collectors.toList());

    }

    public Collection<String> getClusterNames(MqQueueManager queueManager) {

        PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_NAMELIST);
        request.addParameter(MQConstants.MQCA_NAMELIST_NAME, "NL.*");
        log.debug("getting cluster names");
        PCFMessage[] responses = execute(queueManager, request);

        List<String> clusternames = new ArrayList<>();

        for (PCFMessage pcfMessage : responses) {
            PCFParameter cluster = pcfMessage.getParameter(MQConstants.MQCA_NAMELIST_NAME);
            clusternames.add(cluster.getStringValue().trim());
        }
        return clusternames;
    }

    /**
     * Get all topics for a queueMananger
     */
    public Collection<MqTopic> getTopics(MqQueueManager queueManager) {
        PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_TOPIC);
        request.addParameter(MQConstants.MQCA_TOPIC_NAME, "*");

        PCFMessage[] responses = execute(queueManager, request);

        return Stream.of(responses)
                .map(response -> new MqTopic(get(response, MQConstants.MQCA_TOPIC_NAME), get(response, MQConstants.MQCA_TOPIC_STRING)))
                .filter(topic -> !topic.getName().startsWith("SYSTEM"))
                .collect(Collectors.toList());
    }

    public void disableTopic(MqQueueManager queueManager, MqTopic topic) {
        PCFMessage disableRequest = new PCFMessage(MQConstants.MQCMD_CHANGE_TOPIC);
        disableRequest.addParameter(MQConstants.MQCA_TOPIC_NAME, topic.getName());
        disableRequest.addParameter(MQConstants.MQIA_INHIBIT_PUB, MQConstants.MQTA_PUB_INHIBITED);
        disableRequest.addParameter(MQConstants.MQIA_INHIBIT_SUB, MQConstants.MQTA_SUB_INHIBITED);
        execute(queueManager, disableRequest);
        log.info("Disabled topic {}", topic.getName());
    }

    public void enableTopic(MqQueueManager queueManager, MqTopic topic) {
        PCFMessage disableRequest = new PCFMessage(MQConstants.MQCMD_CHANGE_TOPIC);
        disableRequest.addParameter(MQConstants.MQCA_TOPIC_NAME, topic.getName());
        disableRequest.addParameter(MQConstants.MQIA_INHIBIT_PUB, MQConstants.MQTA_PUB_AS_PARENT);
        disableRequest.addParameter(MQConstants.MQIA_INHIBIT_SUB, MQConstants.MQTA_SUB_AS_PARENT);
        execute(queueManager, disableRequest);
        log.info("Enabled topic {}", topic.getName());
    }

    public void deleteTopic(MqQueueManager queueManager, MqTopic topic) {
        PCFMessage disableRequest = new PCFMessage(MQConstants.MQCMD_DELETE_TOPIC);
        disableRequest.addParameter(MQConstants.MQCA_TOPIC_NAME, topic.getName());
        execute(queueManager, disableRequest);
        log.info("Deleted topic {}", topic.getName());
    }
    private Boolean isQueueEmpty(MqQueueManager queueManager, String name) {
        if (getQueueDepth(queueManager, name) != 0) {
            return false;
        }
        return true;
    }

    private Integer getQueueDepth(MqQueueManager queueManager, String name) {
        PCFMessage request;
        PCFMessage[] response;
        int depth = 0;

        request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q_STATUS);
        request.addParameter(MQConstants.MQCA_Q_NAME, name);
        request.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_LOCAL);
        request.addParameter(MQConstants.MQIACF_Q_STATUS_TYPE, MQConstants.MQIACF_Q_STATUS);
        request.addParameter(MQConstants.MQIACF_Q_STATUS_ATTRS, new int [] { MQConstants.MQIA_CURRENT_Q_DEPTH });
        response = execute(queueManager, request);
        try {
            log.info("Getting queue depth from response {}", response.length);
            if (response.length == 1) {
                if (((response[0]).getCompCode() == MQConstants.MQCC_OK) &&
                        ((response[0]).getParameterValue(MQConstants.MQCA_Q_NAME) != null)) {
                    depth = response[0].getIntParameterValue(MQConstants.MQIA_CURRENT_Q_DEPTH);
                    log.info("Queue depth is {} for {}", depth, name);
                }
            }
        } catch (PCFException pcfException) {
            log.error("Get queue depth failed: ", pcfException);
        }

        return depth;
    }

    private String get(PCFMessage pcf, int param) {
        return pcf.getParameter(param).getStringValue().trim();
    }

    private PCFMessage[] execute(MqQueueManager queueManager, PCFMessage request) {
        PCFMessage[] message;
        try {
            queueManager.connect();
            message = queueManager.execute(request);
        } finally {
            queueManager.close();
        }
        return message;
    }

    public Map<EnvironmentClass, MqAdminUser> getCredentialMap() {
        return credentialMap;
    }

}
