package no.nav.aura.basta;

public enum EnvironmentClass {
    u, t, q, p;

    public static EnvironmentClass from(Enum<?> e) {
        return valueOf(e.name().substring(0, 1));
    }
}
