package no.nav.aura.basta.backend.certificate.ad;

import java.security.KeyStore;

public class ServiceUserAccount {

    private String applicationName;
    private String password;
    private KeyStore keyStore;
    private String keyStoreAlias;
    private String keyStorePassword;
    private String domain;
    private String environmentClass;

    public ServiceUserAccount(String applicationName, String domain) {
        this.applicationName = applicationName;
        this.domain = domain;

        switch (domain)
        {
        case "oera-t.local":
            setEnvironmentClass("t");
            break;
        case "test.local":
            setEnvironmentClass("t");
            break;
        case "devillo.no":
            setDomain("test.local");
            setEnvironmentClass("u");
            break;
        case "preprod.local":
            setEnvironmentClass("q");
            break;
        case "oera-q.local":
            setEnvironmentClass("q");
            break;
        case "adeo.no":
            setEnvironmentClass("p");
            break;
        case "oera.no":
            setEnvironmentClass("p");
            break;
        default:
            throw new IllegalArgumentException("Unknown domain " + domain);
        }

    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public String getKeyStoreAlias() {
        return keyStoreAlias;
    }

    public void setKeyStoreAlias(String keyStoreAlias) {
        this.keyStoreAlias = keyStoreAlias;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getDomain() {
        return domain;
    }

    public String getAlias() {
        return "srv" + applicationName.toLowerCase();
    }

    public String getUserAccountName() {
        String userName = "srv" + applicationName;
        if (applicationName.length() > 17) {
            userName = "srv" + applicationName.substring(0, 15).toLowerCase();
        }

        if ("u".equals(environmentClass)) {
            if (userName.length() > 16) {
                userName = userName.substring(0, 16);
            }
            userName = userName + "_u";
        }
        return userName;

    }

    public String getUserFQDN() {
        return "cn=" + getUserAccountName() + ",OU=ApplAccounts,OU=ServiceAccounts,DC=" + getDomain().split("\\.")[0] + ",DC=" + getDomain().split("\\.")[1];
    }

    public String getServerFQDN(String serverName) {
        return "cn=" + serverName + "." + getDomain() + ",O=NAV,L=Oslo,S=Oslo,C=NO";
    }

    public String getSearchBase() {
        return "OU=ApplAccounts,OU=ServiceAccounts,DC=" + getDomain().split("\\.")[0] + ",DC=" + getDomain().split("\\.")[1];
    }

    public String getBaseDN() {
        return "DC=" + getDomain().split("\\.")[0] + ",DC=" + getDomain().split("\\.")[1];
    }

    public String getEnvironmentClass() {
        return environmentClass;
    }

    private void setEnvironmentClass(String environmentClass) {
        this.environmentClass = environmentClass;
    }

    public void setDomain(String domain) {
        this.domain = domain;

    }
}
