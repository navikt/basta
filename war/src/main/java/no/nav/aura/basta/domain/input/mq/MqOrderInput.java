package no.nav.aura.basta.domain.input.mq;

import java.util.Map;

import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Input;

public class MqOrderInput extends MapOperations implements Input {

    public static final String ENVIRONMENT_CLASS = "environmentClass";
    public static final String ENVIRONMENT_NAME = "environmentName";
	public static final String APPLICATION = "application";
	public static final String QUEUE_MANAGER = "queueManager";
	public static final String ALIAS = "fasitAlias";
	public static final String MQ_NAME = "mqQueueName";
	public static final String DESCRIPTION = "description";
	public static final String QUEUE_DEPTH = "queueDepth";
	public static final String MAX_MESSAGE_SIZE = "maxMessageSize";
	
	
	public MqOrderInput(Map<String, String> map) {
        super(map);
    }

	public EnvironmentClass getEnvironmentClass() {
        return getEnumOrNull(EnvironmentClass.class, ENVIRONMENT_CLASS);
    }

    public String getEnvironmentName() {
        return get(ENVIRONMENT_NAME);
    }
    
    public String getAppliation() {
        return get(APPLICATION);
    }

    public String getQueueManager() {
        return get(QUEUE_MANAGER);
    }

    public String getAlias() {
        return get(ALIAS);
    }

    public String getMqName() {
        return get(MQ_NAME);
    }

    public String getDescription() {
        return get(DESCRIPTION);
    }
    
    public Integer getQueueDepth() {	
        return getIntOrNull(QUEUE_DEPTH);
    }

    public Integer getMaxMessageSize() {	
        return getIntOrNull(MAX_MESSAGE_SIZE);
    }
	
    @Override
    public String getOrderDescription() {
        return "Queue";
    }
	
}
