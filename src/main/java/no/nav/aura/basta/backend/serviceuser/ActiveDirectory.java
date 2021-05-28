package no.nav.aura.basta.backend.serviceuser;

import no.nav.aura.basta.domain.input.AdGroupUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

public class ActiveDirectory {

    private static Logger log = LoggerFactory.getLogger(ActiveDirectory.class);

    private final int UF_ACCOUNTDISABLE = 0x0002;
    private final int UF_PASSWD_NOTREQD = 0x0020;
    private final int UF_NORMAL_ACCOUNT = 0x0200;
    private final int UF_DONT_EXPIRE_PASSWD = 0x10000;
    private final int UF_PASSWORD_EXPIRED = 0x800000;
    private int AD_RETRIES = 0;
    private final int MAX_AD_RETRIES = 5;

    private SecurityConfiguration securityConfig;

    public ActiveDirectory() {
        this(new SecurityConfiguration());
    }

    public ActiveDirectory(String operationGroups, String prodOperationGroups, String superUserGroups) {
        this(new SecurityConfiguration());
        System.setProperty("ROLE_USER_GROUPS", "0000-GA-STDAPPS");
        System.setProperty("ROLE_OPERATIONS_GROUPS", operationGroups);
        System.setProperty("ROLE_SUPERUSER_GROUPS", superUserGroups);
        System.setProperty("ROLE_PROD_OPERATIONS_GROUPS", prodOperationGroups);
    }

    public ActiveDirectory(SecurityConfiguration securityConfiguration) {
        securityConfig = securityConfiguration;
    }

    /**
     * Create new serviceAccount if it does not exist or update password on current account
     */
    public <T extends ServiceUserAccount> T createOrUpdate(T userAccount) {
        userAccount.setPassword(generatePassword(22));
        if (!userExists(userAccount)) {
            log.info("User {} does not exist in {}. Creating", userAccount.getUserAccountName(), userAccount.getDomain());
            createUser(userAccount);
        } else {
            log.info("User {} exist in {}. Updating password", userAccount.getUserAccountName(), userAccount.getDomain());
            updatePassword(userAccount);
        }
        return userAccount;
    }

    private void ensureGroupInAd(ServiceUserAccount ldapContextUser, GroupAccount groupAccount) {
        if (groupExists(ldapContextUser, groupAccount.getGroupFqdn())) {
            return;
        }

        try {
            createGroup(groupAccount, ldapContextUser);
        } catch (Exception e) {
            log.error("An error occurred when adding group " + groupAccount.getName() + " to AD", e);
            throw new RuntimeException(e);
        }
        log.info("Group {} created in {}.", groupAccount.getName(), ldapContextUser.getDomain());
    }

    /*
    1. Ensure user is created in AD/LDAP
    2. Ensure (and create if it doesn't exist) the group
    3. Ensure LDAP MQ Extension Attribute on user
    4. Ensure user in group membership
     */
    public void ensureUserInAdGroup(GroupServiceUserAccount userAccount, GroupAccount groupAccount) {
        // Set-up required stuffs
        ensureGroupInAd(userAccount, groupAccount);

        if (AdGroupUsage.MQ.equals(groupAccount.getGroupUsage())) {
            log.info("Setting extension attribute for MQ for user " + userAccount.getUserAccountName());
            addMqLdapExtensionAttributeToUser(userAccount);
        }

        // Perform actual magic of this function
        addMemberToGroup(groupAccount, userAccount);
    }

    private void createUser(ServiceUserAccount userAccount) {
        LdapContext ctx = createContext(userAccount);
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
            closeContext(ctx);
        }
    }

    private void updatePassword(ServiceUserAccount userAccount) {
        LdapContext ctx = createContext(userAccount);
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
            closeContext(ctx);
        }
    }

    public void disable(ServiceUserAccount userAccount) {
        LdapContext ctx = createContext(userAccount);
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
            closeContext(ctx);
        }
    }

    public void enable(ServiceUserAccount userAccount) {
        LdapContext ctx = createContext(userAccount);
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
            closeContext(ctx);
        }
    }

    public void deleteUser(ServiceUserAccount userAccount) {

        LdapContext ctx = createContext(userAccount);
        try {
            String fqName = userAccount.getServiceUserDN();
            ctx.destroySubcontext(fqName);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            closeContext(ctx);
        }
    }

    public void deleteGroup(ServiceUserAccount userAccount, String groupFqdn) {
        LdapContext ctx = createContext(userAccount);
        try {
            ctx.destroySubcontext(groupFqdn);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            closeContext(ctx);
        }
    }

    public boolean groupExists(ServiceUserAccount userAccountForAdContextQuery, String roleDN) {
        LdapContext ctx = createContext(userAccountForAdContextQuery);
        try {
            String filter = "(&(objectClass=group))";
            SearchControls ctls = new SearchControls();
            log.info("Searching for group: " + roleDN);
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
            closeContext(ctx);
        }
    }

    public boolean userExists(ServiceUserAccount userAccount) {
        return getUser(userAccount).isPresent();
    }

    public Optional<SearchResult> getUser(ServiceUserAccount userAccount) {
        LdapContext ctx = createContext(userAccount);
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
            closeContext(ctx);
        }
    }

    private Optional<SearchResult> getUserInGroup(ServiceUserAccount userAccount, String groupDn) {
        for (int retries = 0; retries < MAX_AD_RETRIES; retries++) {
            LdapContext ctx = createContext(userAccount);
            try {
                Thread.sleep(2 * 1000);
                String filter = "(&(objectClass=user)(objectCategory=person)((samAccountName=" + userAccount.getUserAccountName() + ")))";
                SearchControls ctls = new SearchControls();
                ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                NamingEnumeration<SearchResult> answer = ctx.search(groupDn, filter, ctls);

                if (answer.hasMoreElements()) {
                    return Optional.of(answer.nextElement());
                }

                return Optional.empty();
            } catch (NameNotFoundException nnfe) {
                log.info("Group " + groupDn + " not available for modification yet, trying again...");
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                closeContext(ctx);
            }
        }
        return Optional.empty();
    }

    public void createGroup(GroupAccount groupAccount, ServiceUserAccount userAccount) {
        String fqGroupName = groupAccount.getGroupFqdn();
        LdapContext ctx = createContext(userAccount);

        try {
            Attributes attrs = new BasicAttributes(true);
            attrs.put("objectClass", "group");
            attrs.put("cn", groupAccount.getName());
            attrs.put("name", groupAccount.getName());
            attrs.put("samAccountName", groupAccount.getName());
            attrs.put("description", "Group account for MQ auth");

            ctx.createSubcontext(fqGroupName, attrs);

            log.info("Successfully created group: {} ", fqGroupName);

        } catch (Exception e) {
            log.error("An error occurred when creating group ", e);
            throw new RuntimeException(e);
        } finally {
            closeContext(ctx);
        }
    }

    private Optional<Attribute> getExtensionAttribute(ServiceUserAccount user, String extensionAttribute) {
        LdapContext ctx = createContext(user);
        String userDn = user.getServiceUserDN();
        try {
            // Check if extension attribute exists for user, if so return early
            return Optional.of(ctx.getAttributes(userDn).get(extensionAttribute));
        } catch (Exception e) {
            log.error("Could not get attributes for user " + userDn);
            return Optional.empty();
        } finally {
            closeContext(ctx);
        }
    }

    private void addMqLdapExtensionAttributeToUser(ServiceUserAccount user) {
        String mqAttribute = "extensionAttribute9";
        if (getExtensionAttribute(user, mqAttribute).isPresent()) {
            log.info("Extension attribute has already been set.");
            return;
        }

        String userDn = user.getServiceUserDN();
        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(mqAttribute, user.getUserAccountExtensionAttribute()));

        for (int retries = 0; !getExtensionAttribute(user, mqAttribute).isPresent(); retries++) {
            log.debug("Attempting to add extension attribute to " + userDn + ", this is attempt number " + (retries + 1) + ".");
            LdapContext ctx = createContext(user);
            try {
                Thread.sleep(1*1000);
                ctx.modifyAttributes(userDn, mods);
                log.info("Attribute " + mqAttribute + " set for user " + userDn);
                break;
            } catch (NameNotFoundException nnfe) {
                log.debug("User " + userDn + " not available for modification yet, trying again...");
                if (retries > MAX_AD_RETRIES) {
                    throw new RuntimeException(nnfe);
                }
            } catch (Exception e) {
                log.error("An error occurred when adding MQ LDAP Attribute to user", e);
                throw new RuntimeException(e);
            }
            finally {
                closeContext(ctx);
            }
        }
    }

    private void addMemberToGroup(GroupAccount groupAccount, ServiceUserAccount userAccount) {
        String groupDn = groupAccount.getGroupFqdn();
        String userDn = userAccount.getServiceUserDN();
        BasicAttributes attrs = new BasicAttributes();
        attrs.put("member", userDn);

        for (int retries = 0; !userExistsInGroup(userAccount, groupDn); retries++) {
            LdapContext ctx = createContext(userAccount);
            log.debug("Attempting to add " + userAccount.getUserAccountName() + " to " + groupDn + ", attempt number " + (retries + 1) + ".");
            try {
                Thread.sleep(1*1000);
                ctx.modifyAttributes(groupDn, DirContext.ADD_ATTRIBUTE, attrs);
                log.info("User " + userDn + " added to group " + groupAccount.getName());
                break;
            } catch (NameNotFoundException nnfe) {
                log.debug("Group " + groupDn + " not available for modification yet, trying again...");
                if (retries > MAX_AD_RETRIES) {
                    throw new RuntimeException(nnfe);
                }
            } catch (Exception e) {
                log.error("An error occurred when adding member " + userDn + " to group " + groupDn, e);
                throw new RuntimeException(e);
            }
            finally {
                closeContext(ctx);
            }

        }
    }

    private boolean userExistsInGroup(ServiceUserAccount userAccount, String groupDn) {
        return getUserInGroup(userAccount, groupDn).isPresent();
    }

    private javax.naming.ldap.LdapContext createContext(ServiceUserAccount userAccount) {
        // Create the initial directory context
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();

            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.SECURITY_PROTOCOL, "ssl");
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            SecurityConfigElement securityDomain = securityConfig.getConfigForDomain(userAccount.getDomain());
            env.put(Context.SECURITY_PRINCIPAL, securityDomain.getUsername());
            env.put(Context.SECURITY_CREDENTIALS, securityDomain.getPassword());

            // connect to my domain controller
            env.put(Context.PROVIDER_URL, securityDomain.getLdapUrl().toString());
            log.debug("Created ldap context " + securityDomain.getLdapUrl() + " for " + userAccount.getUserAccountName());
            return new InitialLdapContext(env, null);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeContext(javax.naming.ldap.LdapContext ctx) {
        try {
            ctx.close();
        } catch (Exception e) {
            log.error("Error closing context {}", e.getMessage());
        }
    }

    private String generatePassword(int length) {
        return PasswordGenerator.generate(length);
    }
}
