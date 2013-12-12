package no.nav.aura.basta.persistence;

// TODO rename to the same as Fasit Rest Client 
public enum ApplicationServerType {
    jb(null), wa("was");

    private final String role;

    private ApplicationServerType(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}