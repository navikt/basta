package no.nav.aura.basta.domain.input;

import java.util.Arrays;
import java.util.List;

import no.nav.aura.basta.backend.fasit.deprecated.payload.Zone;

public enum Domain {
    Devillo("devillo.no", EnvironmentClass.u, Zone.fss),
    DevilloSBS("devillo.no", EnvironmentClass.u, Zone.sbs, Zone.dmz),
    TestLocal("test.local", EnvironmentClass.t, Zone.fss),
    OeraT("oera-t.local", EnvironmentClass.t, Zone.sbs, Zone.dmz),
    PreProd("preprod.local", EnvironmentClass.q, Zone.fss),
    OeraQ("oera-q.local", EnvironmentClass.q, Zone.sbs, Zone.dmz),
    Adeo("adeo.no", EnvironmentClass.p, Zone.fss),
    Oera("oera.no", EnvironmentClass.p, Zone.sbs, Zone.dmz),
    iApp("adeo.no", EnvironmentClass.p, Zone.iapp);

    private final String fullyQualifiedDomainName;
    private final EnvironmentClass envClass;
    private List<Zone> zones;

    private Domain(String fqdn, EnvironmentClass envClass, Zone... zone) {
        this.fullyQualifiedDomainName = fqdn;
        this.envClass = envClass;
        this.zones = Arrays.asList(zone);
    }

    public static Domain findBy(EnvironmentClass envClass, Zone zone) {
        for (Domain domain : values()) {
            if (domain.zones.contains(zone) && domain.getEnvironmentClass().equals(envClass)) {
                return domain;
            }
        }
        throw new IllegalArgumentException("domain for " + envClass + ":" + zone + " not found");
    }



    public String getFqn() {
        return fullyQualifiedDomainName;
    }

    public EnvironmentClass getEnvironmentClass() {
        return envClass;
    }

    public static Domain fromFqdn(String name) {
        for (Domain d : values()) {
            if (d.getFqn().equalsIgnoreCase(name)) {
                return d;
            }
        }
        throw new IllegalArgumentException("Domain with name not found: " + name);
    }
}
