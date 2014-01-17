package no.nav.aura.basta.rest;

public enum OrderStatus {
    PROCESSING(false), SUCCESS(true), FAILURE(true), NEW(false), ERROR(false);

    private final boolean terminated;

    OrderStatus(boolean terminated) {
        this.terminated = terminated;
    }

    public boolean isTerminated() {
        return terminated;
    }

}
