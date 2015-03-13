package no.nav.aura.basta.domain.input.serviceuser;

import java.util.List;

import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.util.SerializablePredicate;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public enum Domain {
    Devillo("devillo.no", EnvironmentClass.u, Zone.fss),
    TestLocal("test.local", EnvironmentClass.t, Zone.fss),
    OeraT("oera-t.local", EnvironmentClass.t, Zone.sbs),
    PreProd("preprod.local", EnvironmentClass.q, Zone.fss),
    OeraQ("oera-q.local", EnvironmentClass.q, Zone.sbs),
    Adeo("adeo.no", EnvironmentClass.p, Zone.fss),
    Oera("oera.no", EnvironmentClass.p, Zone.sbs);

    private final String fullyQualifiedDomainName;
    private final EnvironmentClass envClass;
    private Zone zone;

    private Domain(String fqdn, EnvironmentClass envClass, Zone zone) {
        this.fullyQualifiedDomainName = fqdn;
        this.envClass = envClass;
        this.zone = zone;
    }

    public static Domain findBy(EnvironmentClass envClass, Zone zone) {
        for (Domain domain : values()) {
            if (zone.equals(domain.getZone()) && envClass.equals(domain.getEnvironmentClass())) {
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
