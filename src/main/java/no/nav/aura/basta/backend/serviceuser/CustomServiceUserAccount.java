package no.nav.aura.basta.backend.serviceuser;

import no.nav.aura.basta.backend.fasit.deprecated.payload.Zone;
import no.nav.aura.basta.domain.input.EnvironmentClass;

public class CustomServiceUserAccount extends ServiceUserAccount{
    private String customUserAccountName;

    public CustomServiceUserAccount(EnvironmentClass environmentClass, Zone zone, String userAccountName) {
        super(environmentClass, zone);
        this.customUserAccountName = userAccountName;
    }

    @Override
    public String getUserAccountName() {
        return customUserAccountName;
    }

    public String getVaultCredsPath() {
        return super.getVaultCredsPath(customUserAccountName);
    }
}
