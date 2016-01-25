package no.nav.aura.basta.backend.mq;

import no.nav.aura.basta.domain.input.EnvironmentClass;

public class MqAdminUser {
  
    private String username;
    private String password;
    private String channelName;
    
    public MqAdminUser(String username, String password, String channelName) {
        this.username = username;
        this.password = password;
        this.channelName = channelName;
    }
    
    public static MqAdminUser from(EnvironmentClass environmentClass){
        //TODO
        final String channel = "SRVAURA.ADMIN";
        final String username = "mq.user." + environmentClass + ".username";
        final String password = "mq.user." + environmentClass + ".password";
        return new MqAdminUser(username, password, channel);
    }
    
    
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getChannelName() {
        return channelName;
    }
    
    @Override
    public String toString() {
        return "MqAdminUser [username=" + username + ", channelName=" + channelName + "]";
    }

}
