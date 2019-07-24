package no.nav.aura.basta.backend.fasit.payload;

public class LifecyclePayload {
    public LifeCycleStatus status;

    public LifecyclePayload withStatus(LifeCycleStatus status) {
        this.status = status;
        return this;
    }
}

