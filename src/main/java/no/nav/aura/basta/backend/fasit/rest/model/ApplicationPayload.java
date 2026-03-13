package no.nav.aura.basta.backend.fasit.rest.model;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApplicationPayload  extends EntityPayload{
    
    @NotNull(message="application name is required")
    public String name;
    @JsonProperty("groupid")
    public String groupId;
    @JsonProperty("artifactid")
    public String artifactId;
    @JsonProperty("portoffset")
    public Integer portOffset;
    
    public ApplicationPayload() {
    }
    
    public ApplicationPayload(String name) {
        this.name = name;
    }

    public ApplicationPayload(String name, String groupId, String artifactId) {
        this.name = name;
        this.groupId = groupId;
        this.artifactId = artifactId;
    }
}
