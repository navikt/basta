package no.nav.aura.basta.rest.dataobjects;

public enum StatusLogLevel {

    error, warning, success, info;

    /** valueOf method with default */
    public static StatusLogLevel valueOfWithDefault(String name) {
        try {
            return (Enum.valueOf(StatusLogLevel.class, name));
        } catch (Exception e) {
            return StatusLogLevel.info;
        }
    }

}
