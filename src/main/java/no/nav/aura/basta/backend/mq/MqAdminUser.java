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
        String envClass = environmentClass.toString().toUpperCase();
        final String channel = System.getProperty("BASTA_MQ_" + envClass + "_CHANNEL", "SRVAURA.ADMIN");
        
        String usernameProperty = "BASTA_MQ_" + envClass + "_USERNAME";
        String username = System.getProperty(usernameProperty);
        if(username==null){
            throw new IllegalArgumentException("Missing required property " + usernameProperty);
        }
        String passwordProperty = "BASTA_MQ_" + envClass + "_PASSWORD";
        String password = System.getProperty(passwordProperty);
        if(password==null){
            throw new IllegalArgumentException("Missing required property " + passwordProperty);
        }
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
