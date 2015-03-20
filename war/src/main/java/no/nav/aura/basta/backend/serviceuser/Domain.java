package no.nav.aura.basta.backend.serviceuser;

import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;

public enum Domain {
    Devillo("devillo.no", EnvironmentClass.u, Zone.fss, "test.local"),
    TestLocal("test.local", EnvironmentClass.t, Zone.fss, "test.local"),
    OeraT("oera-t.local", EnvironmentClass.t, Zone.sbs, "test.local"),
    PreProd("preprod.local", EnvironmentClass.q, Zone.fss, "preprod.local"),
    OeraQ("oera-q.local", EnvironmentClass.q, Zone.sbs, "preprod.local"),
    Adeo("adeo.no", EnvironmentClass.p, Zone.fss, "adeo.no"),
    Oera("oera.no", EnvironmentClass.p, Zone.sbs, "adeo.no");

    private final String fullyQualifiedDomainName;
    private final EnvironmentClass envClass;
    private Zone zone;
    /** LDAP or CA server for this domain */
    private String securityDomain;

    private Domain(String fqdn, EnvironmentClass envClass, Zone zone, String securityDomain) {
        this.fullyQualifiedDomainName = fqdn;
        this.envClass = envClass;
        this.zone = zone;
        this.securityDomain = securityDomain;
    }

    public static Domain findBy(EnvironmentClass envClass, Zone zone) {
        for (Domain domain : values()) {
            if (zone.equals(domain.getZone()) && envClass.equals(domain.getEnvironmentClass())) {
                return domain;
            }
        }
        throw new IllegalArgumentException("domain for " + envClass + ":" + zone + " not found");
    }

    public String getSecurityDomain() {
        return securityDomain;

    }

    public String getFqn() {
        return fullyQualifiedDomainName;
    }

    public EnvironmentClass getEnvironmentClass() {
        return envClass;
    }

    public boolean isInZone(Zone zone) {
        return this.zone.equals(zone);
    }

    public static Domain fromFqdn(String name) {
        for (Domain d : values()) {
            if (d.getFqn().equalsIgnoreCase(name)) {
                return d;
            }
        }
        throw new IllegalArgumentException("Domain with name not found: " + name);
    }

    public Zone getZone() {
        return zone;
    }

    public String getNameWithZone() {
        return String.format("%s (%s)", fullyQualifiedDomainName, zone);
    }
}
