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

    public static OrderStatus fromString(String option){
        switch (option){
            case "error" : return ERROR;
            case "success" : return SUCCESS;
            default: return PROCESSING;
        }
    }
}
