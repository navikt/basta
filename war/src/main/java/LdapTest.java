import java.util.Date;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class LdapTest {
    @SuppressWarnings("restriction")
    public static void main(String[] args) throws Exception {
        // final String ldapAdServer = "ldap://ldapgw.adeo.no";
        // final String ldapAdServer = "ldap://ldapgw.test.local";
        final String ldapAdServer = "ldap://ldapgw.devillo.no";
        // final String ldapSearchBase = "ou=NAV,ou=BusinessUnits,dc=test,dc=local";
        // final String ldapSearchBase = "ou=NAV,ou=BusinessUnits,dc=adeo,dc=no";
        // final String ldapSearchBase = "OU=ApplAccounts,OU=ServiceAccounts,dc=adeo,dc=no";
        // final String ldapSearchBase = "OU=ApplAccounts,OU=ServiceAccounts,dc=test,dc=local";
        final String ldapSearchBase = "OU=ApplAccounts,OU=ServiceAccounts,dc=devillo,dc=no";
        // final String ldapSearchBase = "dc=adeo,dc=no";

        // final String ldapUsername = "srvSSOLinux";
        // final String ldapPassword = "khFKL0GCusWe72";
        // final String ldapUsername = "srvOmnideptool";
        // final String ldapPassword = "SqXeGF0LLIXvfiI";
        // final String ldapUsername = "e137012@adeo.no";
        final String ldapUsername = "srvKodeverk-triple";
        final String ldapPassword = new String(System.console().readPassword());

        // final String ldapAccountToLookup = "e137012";
        // final String ldapAccountToLookup = "srvKodeverk";
        final String ldapAccountToLookup = "srvKodeverk-triple";

        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        if (ldapUsername != null) {
            env.put(Context.SECURITY_PRINCIPAL, ldapUsername);
        }
        if (ldapPassword != null) {
            env.put(Context.SECURITY_CREDENTIALS, ldapPassword);
        }
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.INITIAL_CONTEXT_FACTORY, com.sun.jndi.ldap.LdapCtxFactory.class.getName());
        env.put(Context.PROVIDER_URL, ldapAdServer);

        // ensures that objectSID attribute values
        // will be returned as a byte[] instead of a String
        // env.put("java.naming.ldap.attributes.binary", "objectSID");

        // the following is helpful in debugging errors
        // env.put("com.sun.jndi.ldap.trace.ber", System.err);

        LdapContext ctx = new InitialLdapContext(env, new Control[0]);

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<SearchResult> searchResults = ctx.search(ldapSearchBase, "(cn=" + ldapAccountToLookup + ")", searchControls);

        for (; searchResults.hasMore();) {
            SearchResult searchResult = searchResults.next();
            System.out.println("Found: " + searchResult.getName());
            NamingEnumeration<? extends Attribute> attributes = searchResult.getAttributes().getAll();
            for (; attributes.hasMore();) {
                Attribute attribute = attributes.next();
                System.out.println("  Attr: " + attribute.getID());
                NamingEnumeration<?> values = attribute.getAll();
                for (; values.hasMore();) {
                    Object value = values.next();
                    switch (attribute.getID()) {
                    case "lastLogon":
                    case "badPasswordTime":
                    case "lastLogonTimestamp":
                        value = new Date(Long.parseLong((String) value));
                    default:
                    }
                    System.out.println("    Value: " + value);
                }
            }
        }
    }
}
