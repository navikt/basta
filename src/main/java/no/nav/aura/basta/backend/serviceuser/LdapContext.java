package no.nav.aura.basta.backend.serviceuser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import java.util.Hashtable;
import java.util.Optional;

public class LdapContext {

    private static Logger log = LoggerFactory.getLogger(LdapContext.class);

    private final int UF_ACCOUNTDISABLE = 0x0002;
    private final int UF_PASSWD_NOTREQD = 0x0020;
    private final int UF_NORMAL_ACCOUNT = 0x0200;
    private final int UF_DONT_EXPIRE_PASSWD = 0x10000;
    private final int UF_PASSWORD_EXPIRED = 0x800000;

    private SecurityConfiguration securityConfig;

    public LdapContext(SecurityConfiguration securityConfiguration) {
        securityConfig = securityConfiguration;
    }

    protected javax.naming.ldap.LdapContext createContext(ServiceUserAccount userAccount) {
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
            log.info("Created ldap context " + securityDomain.getLdapUrl() + " for " + userAccount.getUserAccountName());
            return new InitialLdapContext(env, null);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    protected void closeContext(javax.naming.ldap.LdapContext ctx) {
        try {
            ctx.close();
        } catch (Exception e) {
            log.error("Error closing context {}", e.getMessage());
        }
    }
}
