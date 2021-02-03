package no.nav.aura.basta.backend.serviceuser;

import no.nav.aura.basta.backend.fasit.payload.Zone;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.EnvironmentClass;

abstract public class ServiceUserAccount {
    private String password;
    private Domain domain;
    protected EnvironmentClass environmentClass;
    private Boolean hasStsAccess;
    private Boolean hasAbacAccess;

    public ServiceUserAccount(EnvironmentClass environmentClass, Zone zone) {
        this.environmentClass = environmentClass;
        this.domain = Domain.findBy(environmentClass, zone);
    }

    public abstract String getUserAccountName();

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Domain getDomain() {
        return domain;
    }

    public String getDomainFqdn() {
        return domain.getFqn();
    }

    public String getServiceUserSearchBase() {
        return "OU=ApplAccounts,OU=ServiceAccounts," + getBaseDN();
    }

    public String getBaseDN() {
        String[] securityDomain = getSecurityDomain().getFqn().split("\\.");
        return "DC=" + securityDomain[0] + ",DC=" + securityDomain[1];
    }

    /** Finner hvilket sikkerhetsdomene denne brukern er knyttet til. Det er unntak for u */
    private Domain getSecurityDomain() {
        if (environmentClass == EnvironmentClass.u) {
            return Domain.TestLocal;
        }
        return domain;
    }

    public EnvironmentClass getEnvironmentClass() {
        return environmentClass;
    }

    public Boolean getHasAbacAccess() {
        return hasAbacAccess;
    }

    public Boolean getHasStsAccess() {
        return hasStsAccess;
    }

    public void setStsAccess(Boolean access) { this.hasStsAccess = access; }

    public void setAbacAccess(Boolean access) { this.hasAbacAccess = access; }

    public String getServiceUserDN() {
        return "cn=" + getUserAccountName() + "," + getServiceUserSearchBase();
    }

    String getVaultCredsPath(String userAccountName) {
        String env;
        switch (getEnvironmentClass()) {
            case p:
                env = "prod";
                break;
            case q:
                env = "dev";
                break;
            default:
                env = "test";
                break;
        }

        final String usernameLowercase = userAccountName.toLowerCase();
        String vaultCredentialsPath = "serviceuser/" + env + "/" + usernameLowercase;

        if (
                getDomain() == Domain.DevilloSBS ||
                        getDomain() == Domain.iApp ||
                        getDomain() == Domain.Oera ||
                        getDomain() == Domain.OeraT ||
                        getDomain() == Domain.OeraQ
        ) {
            vaultCredentialsPath += "-sbs";
        }
        if (getEnvironmentClass() == EnvironmentClass.u) {
            vaultCredentialsPath += "-u";
        }
        return vaultCredentialsPath;
    }
}

