package no.nav.aura.basta.backend.serviceuser;

import no.nav.aura.basta.backend.fasit.deprecated.payload.Zone;
import no.nav.aura.basta.domain.input.AdGroupUsage;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.EnvironmentClass;

public class GroupAccount {

    private Domain domain;
    protected EnvironmentClass environmentClass;
    private AdGroupUsage groupUsage;
    private String name;

    public GroupAccount(EnvironmentClass environmentClass, Zone zone, String applicationName) {
        this.domain = Domain.findBy(environmentClass, zone);
        this.environmentClass = environmentClass;
        this.name = applicationName;
    }

    public String getName() {
        return name;
    }

    public void setName(String applicationName) {
        this.name = getNamePrefix() + applicationName.toUpperCase();
    }

    public AdGroupUsage getGroupUsage() {
        return groupUsage;
    }

    public void setGroupUsage(AdGroupUsage groupUsage) {
        this.groupUsage = groupUsage;
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

    public String getNamePrefix() {
        String prefix = "0000-GA-";
        if (AdGroupUsage.MQ.equals(getGroupUsage())) {
            prefix += "MQ-";
        }
        return prefix;
    }

}
