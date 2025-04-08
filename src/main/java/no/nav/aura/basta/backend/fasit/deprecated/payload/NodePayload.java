package no.nav.aura.basta.backend.fasit.deprecated.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class NodePayload {
    public String hostname;
    public String environmentClass;
    public String environment;
    public PlatformType type;

    public String username;
    public SecretPayload password;
    public Zone zone;

    public NodePayload() {}

    public NodePayload(PlatformType type) {
        this.type = type;
    }

    public NodePayload withHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public NodePayload withEnvironmentClass(String environmentClass) {
        this.environmentClass = environmentClass;
        return this;
    }

    public NodePayload withEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public NodePayload withZone(Zone zone ) {
        this.zone = zone;
        return this;
    }
}
