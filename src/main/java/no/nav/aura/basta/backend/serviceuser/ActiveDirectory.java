package no.nav.aura.basta.backend.serviceuser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;
import java.util.Optional;

public class ActiveDirectory {

    private static Logger log = LoggerFactory.getLogger(ActiveDirectory.class);

    private final int UF_ACCOUNTDISABLE = 0x0002;
    private final int UF_PASSWD_NOTREQD = 0x0020;
    private final int UF_NORMAL_ACCOUNT = 0x0200;
    private final int UF_DONT_EXPIRE_PASSWD = 0x10000;
    private final int UF_PASSWORD_EXPIRED = 0x800000;

    private SecurityConfiguration securityConfig;

    public ActiveDirectory(){
        this(new SecurityConfiguration());
    }

    public ActiveDirectory(String operationGroups, String prodOperationGroups, String
            superUserGroups){
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
    public ServiceUserAccount createOrUpdate(ServiceUserAccount userAccount) {

        String password = PasswordGenerator.generate(15);
        userAccount.setPassword(password);
        if (!userExists(userAccount)) {
            log.info("User {} does not exist in {}. Creating", userAccount.getUserAccountName(), userAccount.getDomain());
            createUser(userAccount);
        } else {
            System.out.println("update");
            log.info("User {} exist in {}. Updating password", userAccount.getUserAccountName(), userAccount.getDomain());
            updatePassword(userAccount);
        }
        return userAccount;
    }

    private void createUser(ServiceUserAccount userAccount) {

        LdapContext ctx = createContext(userAccount);
        try {
            String fqName = userAccount.getServiceUserDN();
            String roleDN = "cn=RA_Allow_To_Sign_Consumer,ou=Delegation," + userAccount.getBaseDN();

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

            ModificationItem member[] = new ModificationItem[1];

            if (groupExists(userAccount) && userAccount.getDomainFqdn().contains("oera")) {
                log.info("Adding " + userAccount.getUserAccountName() + " to " + roleDN);
                member[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("member", fqName));
                ctx.modifyAttributes(roleDN, member);
            }

            log.info("Successfully created user: {} ", fqName);

        } catch (Exception e) {
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

    public void delete(ServiceUserAccount userAccount) {

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

    private LdapContext createContext(ServiceUserAccount userAccount) {
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
            return new InitialLdapContext(env, null);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean groupExists(ServiceUserAccount userAccount) {

        LdapContext ctx = createContext(userAccount);
        try {
            String searchBase = "cn=RA_Allow_To_Sign_Consumer,ou=Delegation," + userAccount.getBaseDN();
            String filter = "(&(objectClass=group))";
            SearchControls ctls = new SearchControls();
            log.debug("Searching for group: " + searchBase);
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> answer = ctx.search(searchBase, filter, ctls);

            return answer.hasMoreElements();
        } catch (NamingException e) {
            throw new RuntimeException(e);
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

    private void closeContext(LdapContext ctx) {
        try {
            ctx.close();
        } catch (Exception e) {
            log.error("Error closing context {}", e.getMessage());
        }
    }
}
