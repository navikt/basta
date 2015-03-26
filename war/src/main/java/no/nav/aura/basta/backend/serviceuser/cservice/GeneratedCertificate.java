package no.nav.aura.basta.backend.serviceuser.cservice;

import java.security.KeyStore;

import no.nav.aura.basta.backend.serviceuser.ServiceUserAccount;

public class GeneratedCertificate {

    private KeyStore keyStore;
    private String keyStoreAlias;
    private String keyStorePassword;

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public void setKeyStoreAlias(String keystorealias) {
        this.keyStoreAlias = keystorealias;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public String getKeyStoreAlias() {
        return keyStoreAlias;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String generateKeystoreFileName(ServiceUserAccount userAccount) {
        return String.format("%s_%s.jks", userAccount.getUserAccountName(), userAccount.getEnvironmentClass(), userAccount.getDomain());
    }

}
