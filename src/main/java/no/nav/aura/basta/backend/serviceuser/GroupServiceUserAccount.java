package no.nav.aura.basta.backend.serviceuser;

import no.nav.aura.basta.backend.fasit.payload.Zone;
import no.nav.aura.basta.domain.input.EnvironmentClass;

public class GroupServiceUserAccount extends ServiceUserAccount {
    private String applicationName;
    private String userAccountName;

    public GroupServiceUserAccount(EnvironmentClass environmentClass, Zone zone, String applicationName) {
        super(environmentClass, zone);
        this.applicationName = applicationName;
        this.userAccountName = getUserAccountName();
    }

    public String getAlias() {
        return "srv" + applicationName.toLowerCase();
    }

    /**
     * Adding srv to username, lowercase and truncate < 18 to avoid problems with AD
     */
    @Override
    public String getUserAccountName() {
        String userName = "srv" + applicationName;
        if (userName.length() > 18) {
            userName = userName.substring(0, 18);
        }

        if (EnvironmentClass.u.equals(environmentClass)) {
            if (userName.length() > 16) {
                userName = userName.substring(0, 16);
            }
            userName = userName + "_u";
        }
        return userName.toLowerCase();
    }

    /**
     * Lowercase and truncate username to < 12 chars to avoid problems with AD
     */
    @Override
    public String getUserAccountExtensionAttribute() {
        String extensionAttribute = applicationName;
        if (extensionAttribute.length() > 12) {
            extensionAttribute = applicationName.substring(0, 12);
        }

        return extensionAttribute.toLowerCase();
    }

    public String getVaultCredsPath() {
        return super.getVaultCredsPath(userAccountName);
    }
}
