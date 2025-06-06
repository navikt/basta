package no.nav.aura.basta.rest.dataobjects;

import java.util.List;
import java.util.Set;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.security.User;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserDO {

    private String username;
    private String displayName;
    private boolean authenticated;
    private boolean superUser;
    private List<EnvironmentClass> environmentClasses;
    private Set<String> roles;

    public UserDO(User user) {
        this.environmentClasses = user.getEnvironmentClasses();
        this.username = user.getName();
        this.displayName = user.getDisplayName();
        this.authenticated = user.isAuthenticated();
        this.superUser = user.hasSuperUserAccess();
        this.roles = user.getRoles();
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

    public boolean isSuperUser() {
        return superUser;
    }

    public void setSuperUser(boolean superUser) {
        this.superUser = superUser;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
