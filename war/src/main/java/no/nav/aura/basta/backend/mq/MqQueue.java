package no.nav.aura.basta.backend.mq;

import static no.nav.aura.basta.backend.mq.MqChannel.formatChannelName;
import static no.nav.aura.basta.backend.mq.MqChannel.formatRestrictedLength;

import java.util.Date;

public class MqQueue {

    private static final int QUEUENAME_MAX_LENGTH = 45;
    private String name;
    private int maxSizeMb ; // max 100
    private int maxDepth;  
    private String description;


    public MqQueue(String queueName, int maxSizeMb, int maxDepth, String description) {
        this.name = queueName;
        this.maxSizeMb = maxSizeMb;
        this.maxDepth = maxDepth;
        this.description = description;
//        validate()
    }



    public boolean isValidQueueName() {
        return name.length() <= QUEUENAME_MAX_LENGTH;
    }

    public String getName() {
        // max 45
        return name.toUpperCase();
    }

    public String getAlias() {
        // max 48
        return "QA." + getName();
    }

    public int getMaxSizeMb() {
        return maxSizeMb;
    }

    public int getMaxDepth() {
        return maxDepth;
    }


    public String getDescription() {
        return description;
    }

    public int getMaxSizeInBytes() {
        return getMaxSizeMb()*1024*1024;
    }

//    public String generateQueueName() {
//        this.name = formatChannelName(environmentName, appName) + "_" + formatRestrictedLength(name, 24).toUpperCase();
//
//    }
}
