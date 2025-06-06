package no.nav.aura.basta.backend.fasit.deprecated.payload;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ScopePayload {
    public String environmentclass;
    public Zone zone;
    public String environment;
    public String application;

    public ScopePayload() {}

    public ScopePayload(String environmentclass ) {
        this.environmentclass = environmentclass;
    }

    public ScopePayload environment(String environment) {
        this.environment = environment;
        return this;
    }

    public ScopePayload zone(Zone zone ) {
        this.zone = zone;
        return this;
    }



    public ScopePayload application(String application) {
        this.application = application;
        return this;
    }
}

