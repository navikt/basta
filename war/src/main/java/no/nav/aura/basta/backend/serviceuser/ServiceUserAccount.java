package no.nav.aura.basta.backend.serviceuser;

import java.security.KeyStore;

import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.domain.input.serviceuser.Domain;

public class ServiceUserAccount {

    private String applicationName;
    private String password;
    private KeyStore keyStore;
    private String keyStoreAlias;
    private String keyStorePassword;
    private Domain domain;
    private EnvironmentClass environmentClass;

    public ServiceUserAccount(EnvironmentClass environmentClass, Zone zone, String applicationName) {
        this.applicationName = applicationName;
        this.environmentClass = environmentClass;
        this.domain = Domain.findBy(environmentClass, zone);
    }

    @Deprecated
    public ServiceUserAccount(String applicationName, String domainFqdn) {
        this.applicationName = applicationName;
        this.domain = Domain.fromFqdn(domainFqdn);
        this.environmentClass = this.domain.getEnvironmentClass();
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

    public Domain getDomain() {
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

    public String getDomainFqdn() {
        return domain.getFqn();
    }

    public String getUserFQDN() {
        return "cn=" + getUserAccountName() + ",OU=ApplAccounts,OU=ServiceAccounts,DC=" + getDomainFqdn().split("\\.")[0] + ",DC=" + getDomainFqdn().split("\\.")[1];
    }

    public String getServerFQDN(String serverName) {
        return "cn=" + serverName + "." + getDomain() + ",O=NAV,L=Oslo,S=Oslo,C=NO";
    }

    public String getSearchBase() {
        return "OU=ApplAccounts,OU=ServiceAccounts,DC=" + getDomainFqdn().split("\\.")[0] + ",DC=" + getDomainFqdn().split("\\.")[1];
    }

    public String getBaseDN() {
        return "DC=" + getDomainFqdn().split("\\.")[0] + ",DC=" + getDomainFqdn().split("\\.")[1];
    }

    public EnvironmentClass getEnvironmentClass() {
        return environmentClass;
    }

}
