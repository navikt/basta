package no.nav.aura.basta.backend.mq;

import com.ibm.mq.constants.MQConstants;

public class MqChannel {
    private String name;
    private String ipRange = "10.*";
    private String userName;
    private String description;
    private int type = MQConstants.MQCHT_SVRCONN;

    public MqChannel(String channelName, String userName, String description) {
        this.name = channelName;
        this.userName = userName;
        this.description = description;
    }

  
    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public String getIpRange() {
        return ipRange;
    }

    public void setIpRange(String ipRange) {
        this.ipRange = ipRange;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserName() {
        return userName;
    }
}
