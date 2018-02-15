package no.nav.aura.basta.backend.serviceuser;

import java.net.URI;

public class SecurityConfigElement {

    private URI signingURL;
    private URI ldapUrl;
    private String username;
    private String password;

    public SecurityConfigElement(URI serverURL, URI ldapUrl, String username, String password) {
        this.signingURL = serverURL;
        this.ldapUrl = ldapUrl;
        this.username = username;
        this.password = password;
    }

    public URI getSigningURL() {
        return signingURL;
    }

    public void setServerURL(URI serverURL) {
        this.signingURL = serverURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public URI getLdapUrl() {
        return ldapUrl;
    }

    public void setLdapUrl(URI ldapUrl) {
        this.ldapUrl = ldapUrl;
    }

}
