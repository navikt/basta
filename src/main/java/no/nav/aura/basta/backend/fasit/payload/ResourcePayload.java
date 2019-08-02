package no.nav.aura.basta.backend.fasit.payload;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ResourcePayload {

    public ResourceType type;
    public String alias;
    public ScopePayload scope;
    public Map<String, String> properties = new HashMap();
    public Map<String, SecretPayload> secrets = new HashMap();
    public Map<String, FilePayload> files = new HashMap();
    public String id;
    public LifecyclePayload lifecycle;

    public ResourcePayload() {
    }

    public ResourcePayload withId(String id) {
        this.id = id;
        return this;
    }

    public ResourcePayload withType(ResourceType type) {
        this.type = type;
        return this;
    }

    public ResourcePayload withAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public ResourcePayload withScope(ScopePayload scope) {
        this.scope = scope;
        return this;
    }

    public ResourcePayload withProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public ResourcePayload withSecret(String key, String vaule) {
        SecretPayload secretPayload = SecretPayload.forValue(vaule);
        this.secrets.put(key, secretPayload);
        return this;

    }

    public ResourcePayload withVaultSecret(String key, String vaultpath) {
        SecretPayload secretPayload = SecretPayload.forVaultPath(vaultpath);
        this.secrets.put(key, secretPayload);
        return this;
    }

    public ResourcePayload withFile(String key, String filename, URI ref) {
        FilePayload filePayload = new FilePayload();
        filePayload.filename = filename;
        filePayload.ref = ref;
        this.files.put(key, filePayload);
        return this;
    }

    public static class SecretPayload {
        public String vaultpath;
        public String value;

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

    public static class FilePayload {
        public String filename;
        public URI ref;
    }
}
