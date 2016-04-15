package no.nav.aura.basta.domain.input.mq;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import no.nav.aura.basta.backend.mq.MqChannel;
import no.nav.aura.basta.backend.mq.MqQueue;
import no.nav.aura.basta.backend.mq.MqTopic;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Input;
import no.nav.aura.basta.security.User;

public class MqOrderInput extends MapOperations implements Input {

    public static final String ENVIRONMENT_CLASS = "environmentClass";
    public static final String ENVIRONMENT_NAME = "environmentName";
    public static final String APPLICATION = "application";
    public static final String QUEUE_MANAGER = "queueManager";
    public static final String ALIAS = "fasitAlias";
    public static final String MQ_QUEUE_NAME = "mqQueueName";
    public static final String MQ_TOPIC_NAME = "mqTopicName";
    public static final String TOPIC_STRING = "topicString";
    public static final String MQ_CHANNEL_NAME = "mqChannelName";
    public static final String DESCRIPTION = "description";
    public static final String QUEUE_DEPTH = "queueDepth";
    public static final String MAX_MESSAGE_SIZE = "maxMessageSize";
    public static final String MQ_ORDER_TYPE = "mqOrderType";
    public static final String USER_NAME = "username";
    public static final String CREATE_BACKOUT_QUEUE = "createBackoutQueue";
    public static final String BACKOUT_THRESHOLD = "backoutThreshold";
    public static final String CLUSTERNAME = "clusterName";

    public MqOrderInput(Map<String, String> map, MQObjectType mqType) {
        super(map);
        this.put(MQ_ORDER_TYPE, mqType.name());
    }

    public EnvironmentClass getEnvironmentClass() {
        return getEnumOrNull(EnvironmentClass.class, ENVIRONMENT_CLASS);
    }

    public String getEnvironmentName() {
        return get(ENVIRONMENT_NAME);
    }

    public void setCreateBQ(boolean boq) {
        put(CREATE_BACKOUT_QUEUE, String.valueOf(boq));
    }

    public Boolean shouldCreateBQ() {
        String createBQ = getOptional(CREATE_BACKOUT_QUEUE).orElse("false");
        return Boolean.valueOf(createBQ);
    }

    public Optional<String> getClusterName() {
        return getOptional(CLUSTERNAME);
    }

    public String getAppliation() {
        return get(APPLICATION);
    }

    public URI getQueueManagerUri() {
        try {
            return new URI(get(QUEUE_MANAGER));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Wrong format on queuemanager uri " + get(QUEUE_MANAGER));
        }
    }

    public void setQueueManager(String queueManager) {
        put(QUEUE_MANAGER, queueManager);
    }

    public MQObjectType getType() {
        return getEnumOrNull(MQObjectType.class, MQ_ORDER_TYPE);
    }

    public String getAlias() {
        return get(ALIAS);
    }

    public void setAlias(String fasitAlias) {
        put(ALIAS, fasitAlias);
    }

    public String getMqQueueName() {
        return get(MQ_QUEUE_NAME);
    }
    
    public String getTopicName() {
        return get(MQ_TOPIC_NAME);
    }
    
    public void setTopicName(String name) {
        put(MQ_TOPIC_NAME, name);
    }
    
    public String getTopicString() {
        return get(TOPIC_STRING);
    }
    
    public void setTopicString(String topicString) {
        put(TOPIC_STRING, topicString);
    }

    public void setMqQueueName(String mqQueueName) {
        put(MQ_QUEUE_NAME, mqQueueName);
    }

    public void setMaxMessageSize(int maxMessageSizeMb) {
        put(MAX_MESSAGE_SIZE, maxMessageSizeMb);
    }

    public void setQueueDepth(int queueDepth) {
        put(QUEUE_DEPTH, queueDepth);
    }

    public Optional<String> getDescription() {
        return getOptional(DESCRIPTION);
    }

    public void setDescription(String description) {
        put(DESCRIPTION, description);
    }

    public void setEnvironment(String environment) {
        put(ENVIRONMENT_NAME, environment);
    }

    public void setApplication(String application) {
        put(APPLICATION, application);
    }

    public void setEnvironmentClass(EnvironmentClass envclass) {
        put(ENVIRONMENT_CLASS, envclass.name());
    }

    public Integer getQueueDepth() {
        return getIntOr(QUEUE_DEPTH, 5000);
    }

    public Integer getBackoutThreshold() {
        return getIntOr(BACKOUT_THRESHOLD, 1);
    }

    public void setBackoutThreshold(int threshold) {
        put(BACKOUT_THRESHOLD, threshold);
    }

    public Integer getMaxMessageSize() {
        return getIntOr(MAX_MESSAGE_SIZE, 4);
    }

    @Override
    public String getOrderDescription() {
        return get(MQ_ORDER_TYPE);
    }

    public String getMqChannelName() {
        return get(MQ_CHANNEL_NAME);
    }

    public Optional<String> getUserName() {
        return getOptional(USER_NAME);
    }

    public MqQueue getQueue() {
        MqQueue mqQueue = new MqQueue(getMqQueueName(), getMaxMessageSize(), getQueueDepth(), getDescription().orElse(generateDescription(User.getCurrentUser())));
        mqQueue.setCreateBackoutQueue(shouldCreateBQ());
        mqQueue.setBackoutThreshold(getBackoutThreshold());
        if (getClusterName().isPresent()) {
            mqQueue.setClusterName(getClusterName().get());
        }
        return mqQueue;
    }
    
    public MqTopic getTopic(){
        String topicName= getOptional(MQ_TOPIC_NAME).orElse(generateTopicName(getTopicString()));
        MqTopic topic= new MqTopic(topicName, getTopicString());
        topic.setDescription(getDescription().orElse(generateDescription(User.getCurrentUser())));
        return topic;
    }

    protected String generateTopicName(String topicString) {
        String environmentName = getEnvironmentName().toUpperCase();
        String topicStringReversed = StringUtils.reverseDelimited(topicString.toUpperCase().replaceAll("/", "."), '.').replace("."+environmentName, "");
        String topicName = String.format("%s_%s_%s", environmentName, getAppliation().toUpperCase(), topicStringReversed);
        String nameWithValidCharacters = topicName.replaceAll("[^A-Z0-9\\._]", "");
        return StringUtils.left(nameWithValidCharacters, 48);
    }

    protected String generateDescription(User currentUser) {
        String description = String.format("%s for %s in %s. Created by %s (%s)", StringUtils.capitalize(getType().name().toLowerCase()), getAppliation(), getEnvironmentName(), currentUser.getName(),
                currentUser.getDisplayName());
        String normalized = Normalizer.normalize(description, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        return StringUtils.left(normalized, 64);
    }

    public MqChannel getChannel() {
        MqChannel channel= new MqChannel(getMqChannelName());
        channel.setUserName(getUserName().orElse("srvappserver"));
        channel.setDescription(getDescription().orElse(generateDescription(User.getCurrentUser())));
        return channel;
    }
}
