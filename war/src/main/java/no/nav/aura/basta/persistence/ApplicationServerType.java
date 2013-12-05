package no.nav.aura.basta.persistence;

public enum ApplicationServerType {
    jb(null), wa("was"); // wps("wps")

    private final String role;

    private ApplicationServerType(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}