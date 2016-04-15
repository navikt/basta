package no.nav.aura.basta.backend.mq;

import com.ibm.mq.constants.MQConstants;

public class MqChannel {
    private String name;
    private String ipRange = "10.*";
    private String userName;
    private String description;
    private int type = MQConstants.MQCHT_SVRCONN;

    public MqChannel(String channelName) {
        this.name = channelName;
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


    public void setName(String name) {
        this.name = name;
    }


    public void setUserName(String userName) {
        this.userName = userName;
    }


    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "MqChannel [name=" + name + ", ipRange=" + ipRange + ", userName=" + userName + ", description=" + description + ", type=" + type + "]";
    }
}
