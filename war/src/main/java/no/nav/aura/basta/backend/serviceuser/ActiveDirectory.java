package no.nav.aura.basta.backend.serviceuser;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveDirectory {

    private static Logger log = LoggerFactory.getLogger(ActiveDirectory.class);

    private final int UF_ACCOUNTDISABLE = 0x0002;
    private final int UF_PASSWD_NOTREQD = 0x0020;
    private final int UF_NORMAL_ACCOUNT = 0x0200;
    private final int UF_DONT_EXPIRE_PASSWD = 0x10000;
    private final int UF_PASSWORD_EXPIRED = 0x800000;

    private String adminPassword;
    private String adminName;

    @Deprecated
    public ActiveDirectory(String adminName, String adminPassword, ServiceUserAccount user) {
        this.adminName = adminName;
        this.adminPassword = adminPassword;
    }

    public ActiveDirectory(AdminUserConfiguration configuration) {
        // TODO
    }

    public ServiceUserAccount create(ServiceUserAccount userAccount) {

        if (!userExists(userAccount)) {
            log.info("User {} does not exist in {}. Creating", userAccount.getUserAccountName(), userAccount.getDomain());
            String password = PasswordGenerator.generate(15);
            userAccount.setPassword(password);
            createUser(userAccount);
            return userAccount;
        } else {
            throw new RuntimeException(String.format("User %s exists in %s", userAccount.getUserAccountName(), userAccount.getDomain()));
        }

    }

    private void createUser(ServiceUserAccount userAccount) {

        LdapContext ctx = createContext(userAccount);
        try {
            String fqName = userAccount.getServiceUserFQDN();
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
            attrs.put("userPrincipalName", userAccount.getUserAccountName() + "@" + userAccount.getDomain());

            attrs.put("userAccountControl", Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWD_NOTREQD + UF_PASSWORD_EXPIRED + UF_ACCOUNTDISABLE));
            ctx.createSubcontext(fqName, attrs);

            log.info("Created disabled account for: {}", fqName);

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

            log.info("Successfully enabled user: {} ", fqName);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeContext(ctx);
        }
    }

    public void delete(ServiceUserAccount userAccount) {

        LdapContext ctx = createContext(userAccount);
        try {
            String fqName = userAccount.getServiceUserFQDN();
            ctx.destroySubcontext(fqName);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            closeContext(ctx);
        }
    }

    public void start(ServiceUserAccount userAccount) {
    }

    public void stop(ServiceUserAccount userAccount) {
    }

    private LdapContext createContext(ServiceUserAccount userAccount) {
        // Create the initial directory context
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();

            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.SECURITY_PROTOCOL, "ssl");
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, adminName);
            env.put(Context.SECURITY_CREDENTIALS, adminPassword);

            // connect to my domain controller
            env.put(Context.PROVIDER_URL, "ldap://ldapgw." + userAccount.getDomain() + ":636");
            return new InitialLdapContext(env, null);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean groupExists(ServiceUserAccount userAccount) {

        LdapContext ctx = createContext(userAccount);
        try {
            String searchBase = "cn=RA_Allow_To_Sign_Consumer,ou=Delegation," + userAccount.getBaseDN();
            String FILTER = "(&(objectClass=group))";
            SearchControls ctls = new SearchControls();
            log.debug("Searching for group: " + searchBase);
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> answer = ctx.search(searchBase, FILTER, ctls);

            return answer.hasMoreElements();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            closeContext(ctx);
        }

    }

    public boolean userExists(ServiceUserAccount userAccount) {

        LdapContext ctx = createContext(userAccount);
        try {
            String searchBase = userAccount.getServiceUserSearchBase();
            String FILTER = "(&(objectClass=user)(objectCategory=person)((samAccountName=" + userAccount.getUserAccountName() + ")))";
            SearchControls ctls = new SearchControls();
            // TODO sjekke om bruker er gyldig
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> answer = ctx.search(searchBase, FILTER, ctls);

            return answer.hasMoreElements();
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
