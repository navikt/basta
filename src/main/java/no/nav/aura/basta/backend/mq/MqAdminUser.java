package no.nav.aura.basta.backend.mq;

import no.nav.aura.basta.domain.input.EnvironmentClass;

import java.util.Map;

import static java.util.Objects.isNull;

public class MqAdminUser {

    private String username;
    private String password;
    private String channelName;

    public MqAdminUser(String username, String password, String channelName) {
        this.username = username;
        this.password = password;
        this.channelName = channelName;
    }
    
    public static MqAdminUser from(EnvironmentClass environmentClass, Map<EnvironmentClass, MqAdminUser>
            credentialMap) {
        MqAdminUser mqAdminUser = credentialMap.get(environmentClass);
        if(isNull(mqAdminUser.getUsername()) || isNull(mqAdminUser.getPassword())){
            throw new IllegalArgumentException("Missing required property for basta mq username/password");
        }
        return mqAdminUser;
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
