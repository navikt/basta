package no.nav.aura.basta.backend.fasit.payload;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApplicationInstancePayload {
    private String id;
    private String application = null;
    private List<ResourcePayload> exposedresources = new ArrayList<>();
    private Map<String, String> cluster;


    public String getId() {
        return id;
    }


    public String getApplication() {
        return application;
    }

    public String clusterRef() {
        return cluster.get("ref");
    }

    public List<ResourcePayload> getExposedresources() {
        return exposedresources;
    }

    public void setExposedresources(List<ResourcePayload> exposedresources) {
        this.exposedresources = exposedresources;
    }

    public ApplicationInstancePayload withAppName(String appName) {
        this.application = appName;
        return this;
    }
}
