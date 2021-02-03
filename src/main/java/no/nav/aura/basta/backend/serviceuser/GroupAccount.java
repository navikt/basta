package no.nav.aura.basta.backend.serviceuser;

import no.nav.aura.basta.backend.fasit.payload.Zone;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.EnvironmentClass;

public class GroupAccount {

    private Domain domain;
    protected EnvironmentClass environmentClass;
    private String name;
    private String namePrefix;

    public GroupAccount(EnvironmentClass environmentClass, Zone zone) {
        this.domain = Domain.findBy(environmentClass, zone);;
        this.environmentClass = environmentClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = getNamePrefix() + name.toUpperCase();
    }

    public Domain getDomain() {
        return domain;
    }

    public String getGroupFqdn() {
        return "CN=" + getName() + ",OU=AccountGroupNotInRemedy,OU=Groups,OU=NAV,OU=BusinessUnits," + getBaseDN();
    }

    public String getBaseDN() {
        String[] securityDomain = getSecurityDomain().getFqn().split("\\.");
        return "DC=" + securityDomain[0] + ",DC=" + securityDomain[1];
    }

    /**
     * Finner hvilket sikkerhetsdomene denne gruppen er knyttet til. Det er unntak for u
     */
    private Domain getSecurityDomain() {
        if (environmentClass == EnvironmentClass.u) {
            return Domain.TestLocal;
        }
        return domain;
    }

    public void setNamePrefix(String prefix) {
        this.namePrefix = prefix.toUpperCase();
    }

    public String getNamePrefix() { return namePrefix; }
}
