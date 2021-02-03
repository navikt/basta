package no.nav.aura.basta.backend.serviceuser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.LdapContext;
import java.util.Optional;

public class ActiveDirectory {

    private static Logger log = LoggerFactory.getLogger(ActiveDirectory.class);

    private final int UF_ACCOUNTDISABLE = 0x0002;
    private final int UF_PASSWD_NOTREQD = 0x0020;
    private final int UF_NORMAL_ACCOUNT = 0x0200;
    private final int UF_DONT_EXPIRE_PASSWD = 0x10000;
    private final int UF_PASSWORD_EXPIRED = 0x800000;

    private SecurityConfiguration securityConfig;
    private no.nav.aura.basta.backend.serviceuser.LdapContext lc;

    public ActiveDirectory() {
        this(new SecurityConfiguration());
    }

    public ActiveDirectory(String operationGroups, String prodOperationGroups, String superUserGroups) {
        this(new SecurityConfiguration());
        System.setProperty("ROLE_USER_groups", "0000-GA-STDAPPS");
        System.setProperty("ROLE_OPERATIONS_groups", operationGroups);
        System.setProperty("ROLE_SUPERUSER_groups", superUserGroups);
        System.setProperty("ROLE_PROD_OPERATIONS_groups", prodOperationGroups);
    }

    public ActiveDirectory(SecurityConfiguration securityConfiguration) {
        securityConfig = securityConfiguration;
    }

    /**
     * Create new serviceAccount if it does not exist or update password on current account
     */
    public <T extends ServiceUserAccount> T createOrUpdate(T userAccount) {
        String password = PasswordGenerator.generate(22);
        userAccount.setPassword(password);
        if (!userExists(userAccount)) {
            log.info("User {} does not exist in {}. Creating", userAccount.getUserAccountName(), userAccount.getDomain());
            createUser(userAccount);
        } else {
            log.info("User {} exist in {}. Updating password", userAccount.getUserAccountName(), userAccount.getDomain());
            updatePassword(userAccount);
        }
        return userAccount;
    }

    public GroupAccount createAdGroup(GroupAccount groupAccount, ServiceUserAccount userAccount) {
        if (!groupExists(userAccount, groupAccount.getGroupFqdn())) {
            log.info("Group {} does not exist in {}. Creating", groupAccount.getName(), groupAccount.getDomain());
            createGroup(groupAccount, userAccount);
            addMemberToGroup(groupAccount, userAccount);
        }

        return groupAccount;
    }

    private void createUser(ServiceUserAccount userAccount) {

        LdapContext ctx = lc.createContext(userAccount);
        try {
            String fqName = userAccount.getServiceUserDN();
            String signerRoleDn = "CN=RA_Allow_To_Sign_Consumer,OU=Delegation," + userAccount.getBaseDN();
            String abacRoleDn = "CN=0000-GA-pdp-user,OU=AccountGroups,OU=Groups,OU=NAV,OU=BusinessUnits," +
                                        userAccount.getBaseDN();

            // Create attributes to be associated with the new user
            Attributes attrs = new BasicAttributes(true);

            // These are the mandatory attributes for a user object
            attrs.put("objectClass", "user");
            attrs.put("samAccountName", userAccount.getUserAccountName());
            attrs.put("cn", userAccount.getUserAccountName());

            // These are some optional (but useful) attributes
            attrs.put("givenName", userAccount.getUserAccountName());
            attrs.put("displayName", userAccount.getUserAccountName());
            attrs.put("description", "Service account for " + userAccount.getUserAccountName());
            if ("devillo.no".equals(userAccount.getDomainFqdn()) || "utvikling.local".equals(userAccount.getDomainFqdn())) {
                attrs.put("userPrincipalName", userAccount.getUserAccountName() + "@test.local");
            } else {
                attrs.put("userPrincipalName", userAccount.getUserAccountName() + "@" + userAccount.getDomainFqdn());
            }

            attrs.put("userAccountControl", Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWD_NOTREQD + UF_PASSWORD_EXPIRED + UF_ACCOUNTDISABLE));
            ctx.createSubcontext(fqName, attrs);

            log.debug("Created disabled account for: {}", fqName);

            ModificationItem[] mods = new ModificationItem[2];

            // Replace the "unicdodePwd" attribute with a new value
            // Password must be both Unicode and a quoted string
            String newQuotedPassword = "\"" + userAccount.getPassword() + "\"";
            byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");

            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd", newUnicodePassword));
            mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userAccountControl", Integer.toString(UF_NORMAL_ACCOUNT
                                                                                                                                           + UF_DONT_EXPIRE_PASSWD)));

            ctx.modifyAttributes(fqName, mods);

            if (groupExists(userAccount, signerRoleDn) && (userAccount.getHasStsAccess() || userAccount.getDomainFqdn()
                                                                                                    .contains("oera"))) {
                log.info("Adding " + userAccount.getUserAccountName() + " to " + signerRoleDn);
                ModificationItem member[] = new ModificationItem[1];
                member[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("member",
                        fqName));
                ctx.modifyAttributes(signerRoleDn, member);
            }

            if (groupExists(userAccount, abacRoleDn) && userAccount.getHasAbacAccess()) {
                log.info("Adding " + userAccount.getUserAccountName() + " to " + abacRoleDn);
                ModificationItem member[] = new ModificationItem[1];
                member[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("member",
                        fqName));
                ctx.modifyAttributes(abacRoleDn, member);
            }

            log.info("Successfully created user: {} ", fqName);

        } catch (Exception e) {
            log.error("An error occured when updating AD ", e);
            throw new RuntimeException(e);
        } finally {
            lc.closeContext(ctx);
        }
    }

    private void updatePassword(ServiceUserAccount userAccount) {
        LdapContext ctx = lc.createContext(userAccount);
        try {
            String fqName = userAccount.getServiceUserDN();

            ModificationItem[] mods = new ModificationItem[2];

            // Replace the "unicdodePwd" attribute with a new value
            // Password must be both Unicode and a quoted string
            String newQuotedPassword = "\"" + userAccount.getPassword() + "\"";
            byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");

            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd", newUnicodePassword));
            mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userAccountControl", Integer.toString(UF_NORMAL_ACCOUNT
                                                                                                                                           + UF_DONT_EXPIRE_PASSWD)));

            ctx.modifyAttributes(fqName, mods);

            log.info("Updated password on user: {} ", fqName);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lc.closeContext(ctx);
        }
    }

    public void disable(ServiceUserAccount userAccount) {
        LdapContext ctx = lc.createContext(userAccount);
        try {
            String fqName = userAccount.getServiceUserDN();

            ModificationItem[] mods = new ModificationItem[1];

            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userAccountControl", Integer.toString(
                    UF_NORMAL_ACCOUNT + UF_ACCOUNTDISABLE)));

            ctx.modifyAttributes(fqName, mods);

            log.info("Disabled user: {} ", fqName);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lc.closeContext(ctx);
        }
    }

    public void enable(ServiceUserAccount userAccount) {
        LdapContext ctx = lc.createContext(userAccount);
        try {
            String fqName = userAccount.getServiceUserDN();

            ModificationItem[] mods = new ModificationItem[1];

            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userAccountControl", Integer.toString(
                    UF_NORMAL_ACCOUNT + UF_DONT_EXPIRE_PASSWD)));

            ctx.modifyAttributes(fqName, mods);

            log.info("Disabled user: {} ", fqName);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lc.closeContext(ctx);
        }
    }

    public void delete(ServiceUserAccount userAccount) {

        LdapContext ctx = lc.createContext(userAccount);
        try {
            String fqName = userAccount.getServiceUserDN();
            ctx.destroySubcontext(fqName);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            lc.closeContext(ctx);
        }
    }

    public boolean groupExists(ServiceUserAccount userAccount, String roleDN) {
        LdapContext ctx = lc.createContext(userAccount);
        try {
            String filter = "(&(objectClass=group))";
            SearchControls ctls = new SearchControls();
            log.debug("Searching for group: " + roleDN);
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> answer = ctx.search(roleDN, filter, ctls);

            return answer.hasMoreElements();
        } catch (NamingException e) {
            if (e.getMessage().contains("LDAP: error code 32")) {
                return false;
            } else {
                throw new RuntimeException(e);
            }
        } finally {
            lc.closeContext(ctx);
        }

    }

    public boolean userExists(ServiceUserAccount userAccount) {
        return getUser(userAccount).isPresent();
    }

    public Optional<SearchResult> getUser(ServiceUserAccount userAccount) {
        LdapContext ctx = lc.createContext(userAccount);
        try {
            String searchBase = userAccount.getServiceUserSearchBase();
            String filter = "(&(objectClass=user)(objectCategory=person)((samAccountName=" + userAccount.getUserAccountName() + ")))";
            SearchControls ctls = new SearchControls();
            // TODO sjekke om bruker er gyldig
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> answer = ctx.search(searchBase, filter, ctls);
            if (answer.hasMoreElements()) {
                return Optional.of(answer.nextElement());
            }
            return Optional.empty();

        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            lc.closeContext(ctx);
        }
    }

    public void createGroup(GroupAccount groupAccount, ServiceUserAccount userAccount) {
        String fqGroupName = groupAccount.getGroupFqdn();
        LdapContext ctx = lc.createContext(userAccount);
        try {

            // Create attributes to be associated with the new user
            Attributes attrs = new BasicAttributes(true);
            attrs.put("objectClass", "group");
            attrs.put("cn", groupAccount.getName());
            attrs.put("name", groupAccount.getName());
            attrs.put("samAccountName", groupAccount.getName());
            attrs.put("description", "Group account for MQ auth");

            ctx.createSubcontext(fqGroupName, attrs);

            log.info("Successfully created group: {} ", fqGroupName);

        } catch (Exception e) {
            log.error("An error occured when creating group ", e);
            throw new RuntimeException(e);
        } finally {
            lc.closeContext(ctx);
        }
    }

    private void addMemberToGroup(GroupAccount groupAccount, ServiceUserAccount userAccount) {
        String fqGroupName = groupAccount.getGroupFqdn();
        LdapContext ctx = lc.createContext(userAccount);
        try {
            log.info("Adding " + userAccount.getUserAccountName() + " to " + groupAccount.getName());
            ModificationItem member[] = new ModificationItem[1];
            member[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("member",
                    userAccount.getServiceUserDN()));
            ctx.modifyAttributes(fqGroupName, member);
        } catch (Exception e) {
            log.error("An error occured when adding member to group ", e);
            throw new RuntimeException(e);
        } finally {
            lc.closeContext(ctx);
        }
    }
}
