package no.nav.aura.basta.backend.fasit.deprecated;


import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import no.nav.aura.basta.util.MapBuilder;

public class ResourceProperties {
    private String id = null;
    private HashMap<String, String> properties = new HashMap<>();
    private HashMap<String, String> secrets = new HashMap<>();
    private HashMap<String, String> files = new HashMap<>();

    public ResourceProperties() {}

    public ResourceProperties(String id) {
        this.id = id;
    }

    public ResourceProperties(Map<String, String> properties) {
        putProperties(properties);
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public boolean isEmpty() {
        return properties.isEmpty() && secrets.isEmpty();
    }

    public void setSecret(String key, String secret) {
        secrets.put(key, secret);
    }

    public void deleteSecret(String key) {
        secrets.remove(key);
    }

    public void putFile(String filename, String urlRef) {
        files.put(filename, urlRef);
    }

    public Map<String, String> getFiles() {
        return files;
    }

    public String getFileRef(String filename) {
        return files.get(filename);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public String getSecret(String key) {
        return secrets.get(key);
    }

    public void putSecrets(Map<String, String> props) {
        if (props != null) {
            this.secrets.putAll(props);
        }
    }

    public void putProperties(Map<String, String> props) {
        if (props != null) {
            this.properties.putAll(props);
        }
    }

    public Map<String, String> getProperties() {
        return MapBuilder.stringMapBuilder().putAll(properties).build();
    }

    public Map<String, String> getSecrets() {
        return MapBuilder.stringMapBuilder().putAll(secrets).build();
    }

    public void put(ResourceProperties resourceProperties) {
        this.properties.putAll(resourceProperties.getProperties());
        this.secrets.putAll(resourceProperties.getSecrets());
    }

    public static Properties asProperties(Map<String, String> values) {
        Properties props = new Properties();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (entry.getValue() != null) {
                props.setProperty(entry.getKey(), entry.getValue());
            }
        }
        return props;
    }

    public String getId() {
        return id;
    }
}
