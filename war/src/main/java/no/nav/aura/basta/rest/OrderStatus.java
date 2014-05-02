package no.nav.aura.basta.rest;

public enum OrderStatus {
    PROCESSING("info"), SUCCESS( "success"), FAILURE( "danger"), NEW( "active"), ERROR( "danger");

    private final String bootstrapClass;

    OrderStatus(String bootstrapClass) {
        this.bootstrapClass = bootstrapClass;
    }
    public String getBootstrapClass() {
        return bootstrapClass;
    }

    public static OrderStatus fromString(String option){
        switch (option){
            case "error" : return ERROR;
            case "success" : return SUCCESS;
            default: return PROCESSING;
        }
    }
}
