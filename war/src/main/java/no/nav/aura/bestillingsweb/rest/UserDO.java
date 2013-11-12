package no.nav.aura.bestillingsweb.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.springframework.security.core.Authentication;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserDO {

    private String username;
    private boolean authenticated;

    public UserDO(Authentication authentication) {
        this.username = authentication.getName();
        this.authenticated = !"anonymousUser".equals(username) && authentication.isAuthenticated();
    }

    public UserDO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

}
