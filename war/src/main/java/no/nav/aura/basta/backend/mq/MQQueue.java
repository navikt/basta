package no.nav.aura.basta.backend.mq;

import static no.nav.aura.basta.backend.mq.MQChannel.formatChannelName;
import static no.nav.aura.basta.backend.mq.MQChannel.formatRestrictedLength;

import java.util.Date;

public class MQQueue {

    private static final int QUEUENAME_MAX_LENGTH = 45;
    private String name;

    public MQQueue(String name, String environmentName, String appName) {
        this.name = formatChannelName(environmentName, appName) + "_" + formatRestrictedLength(name, 24).toUpperCase();
    }

    public MQQueue(String queueName) {
        this.name = queueName;
    }

    public boolean isValidQueueName() {
        return name.length() <= QUEUENAME_MAX_LENGTH;
    }

    public String getName() {
        // max 45
        return name;
    }

    public String getAlias() {
        // max 48
        return "QA." + getName();
    }

    public String getDescription() {
        return "generated on " + new Date();
    }

}
