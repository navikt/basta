package no.nav.aura.basta.backend.serviceuser;

import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;

public class ServiceUserAccount {

    private String applicationName;
    private String password;
    private Domain domain;
    private EnvironmentClass environmentClass;

    public ServiceUserAccount(EnvironmentClass environmentClass, Zone zone, String applicationName) {
        this.applicationName = applicationName;
        this.environmentClass = environmentClass;
        this.domain = Domain.findBy(environmentClass, zone);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public Domain getDomain() {
        return domain;
    }

    public String getAlias() {
        return "srv" + applicationName.toLowerCase();
    }

    public String getUserAccountName() {
        String userName = "srv" + applicationName;
        if (applicationName.length() > 17) {
            userName = "srv" + applicationName.substring(0, 15).toLowerCase();
        }

        if (EnvironmentClass.u.equals(environmentClass)) {
            if (userName.length() > 16) {
                userName = userName.substring(0, 16);
            }
            userName = userName + "_u";
        }
        return userName;
    }

    public String getDomainFqdn() {
        return domain.getFqn();
    }

    public String getServiceUserDN() {
        return "cn=" + getUserAccountName() + "," + getServiceUserSearchBase();
    }

    public String getServiceUserSearchBase() {
        return "OU=ApplAccounts,OU=ServiceAccounts," + getBaseDN();
    }

    public String getBaseDN() {
        return "DC=" + getDomainFqdn().split("\\.")[0] + ",DC=" + getDomainFqdn().split("\\.")[1];
    }

    public EnvironmentClass getEnvironmentClass() {
        return environmentClass;
    }

}
