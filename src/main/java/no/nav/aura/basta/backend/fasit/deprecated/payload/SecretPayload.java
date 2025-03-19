package no.nav.aura.basta.backend.fasit.deprecated.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class SecretPayload {
    public String vaultpath;
    public String value;
    public String ref;

    public SecretPayload() {}

    public static SecretPayload forValue(String value) {
        SecretPayload sp = new SecretPayload();
        sp.value = value;
        return sp;
    }

    public static SecretPayload forVaultPath(String vaultpath) {
        SecretPayload sp = new SecretPayload();
        sp.vaultpath = vaultpath;
        return sp;
    }
}
