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
        final String channel = System.getProperty("mqadmin_" + environmentClass + "_channel", "SRVAURA.ADMIN");
        
        String usernameProperty = "mqadmin_" + environmentClass + "_username";
        String username = System.getProperty(usernameProperty);
        if(username==null){
            throw new IllegalArgumentException("Missing required property " + usernameProperty);
        }
        String passwordProperty = "mqadmin_" + environmentClass + "_password";
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
