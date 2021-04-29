package no.nav.aura.basta.backend.serviceuser;

import no.nav.aura.basta.backend.fasit.payload.Zone;
import no.nav.aura.basta.domain.input.EnvironmentClass;

public class GroupServiceUserAccount extends ServiceUserAccount {
    private String applicationName;

    public GroupServiceUserAccount(EnvironmentClass environmentClass, Zone zone, String applicationName) {
        super(environmentClass, zone);
        this.applicationName = applicationName;
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
        if (applicationName.length() > 17) {
            userName = "srv" + applicationName.substring(0, 15);
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
     * Adding srv to username, lowercase and truncate < 12 to avoid problems with AD
     */
    @Override
    public String getUserAccountExtensionAttribute() {
        String extensionAttribute = applicationName;
        if (extensionAttribute.length() > 12) {
            extensionAttribute = applicationName.substring(0, 11);
        }

        return extensionAttribute.toLowerCase();
    }
}
