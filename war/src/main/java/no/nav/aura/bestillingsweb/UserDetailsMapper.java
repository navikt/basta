package no.nav.aura.bestillingsweb;

import java.util.Collection;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

public class UserDetailsMapper extends LdapUserDetailsMapper {

    // private static final String SN_ATTR = "sn";
    // private static final String GIVEN_NAME_ATTR = "givenName";

    /**
     * Get the real name for the user and use that as the username. This will be used to show the name of the user logged in
     * */
    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        // TODO we need the ident to report as owner/orderedBy, but should also had the real name
        String name;
        // if (!isServiceUser(ctx)) {
        // name = ctx.getStringAttributes(GIVEN_NAME_ATTR)[0] + " " + ctx.getStringAttributes(SN_ATTR)[0];
        // } else {
        // Since service users will not have these attributes
        name = username;
        // }
        return super.mapUserFromContext(ctx, name, authorities);
    }

    // private static boolean isServiceUser(DirContextOperations ctx) {
    // return !ctx.attributeExists(GIVEN_NAME_ATTR) || !ctx.attributeExists(SN_ATTR);
    // }

}
