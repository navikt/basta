package no.nav.aura.basta.backend.fasit.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class LifecyclePayload {
    public LifeCycleStatus status;

    public LifecyclePayload(LifeCycleStatus status) {
        this.status = status;
    }
}

