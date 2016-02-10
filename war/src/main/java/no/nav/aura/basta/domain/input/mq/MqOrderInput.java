package no.nav.aura.basta.domain.input.mq;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import no.nav.aura.basta.backend.mq.MqQueue;
import no.nav.aura.basta.backend.mq.MqQueueManager;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Input;

public class MqOrderInput extends MapOperations implements Input {

    public static final String ENVIRONMENT_CLASS = "environmentClass";
    public static final String ENVIRONMENT_NAME = "environmentName";
	public static final String APPLICATION = "application";
	public static final String QUEUE_MANAGER = "queueManager";
	public static final String ALIAS = "fasitAlias";
	public static final String MQ_QUEUE_NAME = "mqQueueName";
	public static final String MQ_CHANNEL_NAME = "mqChannelName";
	public static final String DESCRIPTION = "description";
	public static final String QUEUE_DEPTH = "queueDepth";
	public static final String MAX_MESSAGE_SIZE = "maxMessageSize";
	public static final String MQ_ORDER_TYPE = "mqOrderType";
	public static final String USER_NAME="username";
	public static final String CREATE_BACKOUT_QUEUE= "createBackoutQueue";
	
	
	
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
    
    public Boolean shouldCreateBQ(){
        String createBQ = getOptional(CREATE_BACKOUT_QUEUE).orElse("false");
        return Boolean.valueOf(createBQ);
    }
    
    public String getAppliation() {
        return get(APPLICATION);
    }

    public URI getQueueManagerUri() {
        try {
            return new URI(get(QUEUE_MANAGER));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Wrong format on queuemanager uri " +get(QUEUE_MANAGER) );
        }
    }

    public String getAlias() {
        return get(ALIAS);
    }

    public String getMqName() {
        return get(MQ_QUEUE_NAME);
    }

    public String getDescription() {
        return get(DESCRIPTION);
    }
    
    public Integer getQueueDepth() {	
        return getIntOr(QUEUE_DEPTH, 100);
    }

    public Integer getMaxMessageSize() {	
        return getIntOr(MAX_MESSAGE_SIZE, 1);
    }
	
    @Override
    public String getOrderDescription() {
        return get(MQ_ORDER_TYPE);
    }

    public String getMqChannelName() {
        return get(MQ_CHANNEL_NAME);
    }

    public String getUserName() {
        return get(USER_NAME);
    }

    public MqQueue getQueue() {
        MqQueue mqQueue = new MqQueue(getMqName(), getMaxMessageSize(), getQueueDepth(), getDescription());
        mqQueue.setCreateBackoutQueue(shouldCreateBQ());
        return mqQueue;
    }
    
	
}
