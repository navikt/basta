package no.nav.aura.basta.rest;

public enum OrderStatus {
    PROCESSING(false, "info"), SUCCESS(true, "success"), FAILURE(true, "danger"), NEW(false, "active"), ERROR(false, "danger");

    private final boolean terminated;
    private final String bootstrapClass;

    OrderStatus(boolean terminated, String bootstrapClass) {
        this.terminated = terminated;
        this.bootstrapClass = bootstrapClass;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public String getBootstrapClass() {
        return bootstrapClass;
    }
}
