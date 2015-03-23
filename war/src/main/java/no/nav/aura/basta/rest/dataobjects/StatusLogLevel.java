package no.nav.aura.basta.rest.dataobjects;

public enum StatusLogLevel {

    error, warning, success;

    public static StatusLogLevel from(String name) {
        try {
            return (Enum.valueOf(StatusLogLevel.class, name));
        } catch (Exception e) {
            return StatusLogLevel.success;
        }
    }

}
