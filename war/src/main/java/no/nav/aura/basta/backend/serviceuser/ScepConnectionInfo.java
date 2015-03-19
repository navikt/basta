package no.nav.aura.basta.backend.serviceuser;

public class ScepConnectionInfo {

    private String signingURL;
    private String username;
    private String password;

    public ScepConnectionInfo(String serverURL, String username, String password) {
        this.signingURL = serverURL;
        this.username = username;
        this.password = password;
    }

    public String getSigningURL() {
        return signingURL;
    }

    public void setServerURL(String serverURL) {
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

}
