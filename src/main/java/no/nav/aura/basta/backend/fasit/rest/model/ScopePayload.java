package no.nav.aura.basta.backend.fasit.rest.model;


import jakarta.validation.constraints.NotNull;
import no.nav.aura.basta.backend.fasit.rest.model.infrastructure.Zone;
import no.nav.aura.basta.domain.input.EnvironmentClass;

public class ScopePayload {
    @NotNull(message = "Invalid scope. Environment class is required")
    public EnvironmentClass environmentclass;
    public Zone zone;
    public String environment;
    public String application;

    public ScopePayload() {

    }

    public final ScopePayload environmentClass(EnvironmentClass envClass) {
        this.environmentclass = envClass;
        return this;
    }

    public final ScopePayload environment(String environment) {
        this.environment = environment == null ? null : environment.toLowerCase();
        return this;
    }

    public final ScopePayload zone(Zone zone) {
        this.zone = zone;
        return this;
    }

    public final ScopePayload application(String application) {
        this.application = application;
        return this;
    }


}
