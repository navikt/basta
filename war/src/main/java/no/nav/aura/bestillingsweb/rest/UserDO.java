package no.nav.aura.bestillingsweb.rest;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.bestillingsweb.EnvironmentClass;
import no.nav.aura.bestillingsweb.User;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserDO {

    private String username;
    private boolean authenticated;
    private List<EnvironmentClass> environmentClasses;

    public UserDO(User user) {
        this.environmentClasses = user.getEnvironmentClasses();
        this.username = user.getName();
        this.authenticated = user.isAuthenticated();
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

    public List<EnvironmentClass> getEnvironmentClasses() {
        return environmentClasses;
    }

    public void setEnvironmentClasses(List<EnvironmentClass> environmentClasses) {
        this.environmentClasses = environmentClasses;
    }

}
