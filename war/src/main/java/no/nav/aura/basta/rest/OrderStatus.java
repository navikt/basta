package no.nav.aura.basta.rest;

public enum OrderStatus {
    NEW(false),
    PROCESSING(false),
    SUCCESS(true),
    FAILURE(true),
    ERROR(true),
    WARNING(true);

    private final boolean endstate;

    OrderStatus(boolean endstate) {
        this.endstate = endstate;
    }

    public boolean isEndstate() {
        return endstate;
    }

    public static OrderStatus fromString(String option){
        switch (option){
            case "error" : return ERROR;
            case "success" : return SUCCESS;
            case "warning" : return WARNING;
            default: return PROCESSING;
        }
    }
}
