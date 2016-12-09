package no.nav.aura.basta;

import org.apache.commons.dbcp.BasicDataSource;
import org.eclipse.jetty.plus.jndi.Resource;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class BastaJettyRunner {

    private static final String WEB_SRC = "src/main/webapp";
    protected Server server;

    enum Env {
        U, TESTLOCAL
    }

    public BastaJettyRunner(int port, String overrideDescriptor) {
        server = new Server(port);
        setEnvironmentSpecificProperties();
        setOrchestratorConfigProperties();
        WebAppContext context = getContext(overrideDescriptor);
        server.setHandler(context);

        // Add resources
        try {
            new Resource("java:/jdbc/bastaDB", createDatasource());
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    private void setEnvironmentSpecificProperties() {


        // System.setProperty("fasit.rest.api.url", "https://fasit.adeo.no/conf");
        System.setProperty("fasit.rest.api.url", "https://fasit.adeo.no/conf");
        System.setProperty("fasit:resources_v2.url", "https://fasit.adeo.no/api/v2/resources");
        System.setProperty("fasit:applications_v2.url", "https://fasit.adeo.no/api/v2/applications");
        System.setProperty("fasit:environments_v2.url", "https://fasit.adeo.no/api/v2/environments");

        System.setProperty("srvbasta.username", "mjau");
        System.setProperty("srvbasta.password", "pstpst");

        // System.setProperty("ws.menandmice.url", "http://10.83.3.45/_mmwebext/mmwebext.dll?Soap");
        // System.setProperty("ws.menandmice.username", "user");
        // System.setProperty("ws.menandmice.password", "secret");

        System.setProperty("environment.class", "p");
        System.setProperty("ROLE_USER.groups", "0000-GA-STDAPPS");
        System.setProperty("ROLE_OPERATIONS.groups", "0000-GA-STDAPPS");
        // SUPERUSER ALL THE THINGS
        System.setProperty("ROLE_SUPERUSER.groups", "0000-GA-BASTA_SUPERUSER");
        System.setProperty("ROLE_PROD_OPERATIONS.groups", "0000-ga-env_config_S");

        System.setProperty("scep.test.local.url", "https://certenroll.test.local/certsrv/mscep/");
        System.setProperty("scep.test.local.username", "srvSCEP");
        System.setProperty("scep.test.local.password", "fjas");
        System.setProperty("scep.adeo.no.url", "adeourl");
        System.setProperty("scep.adeo.no.username", "");
        System.setProperty("scep.adeo.no.password", "");
        System.setProperty("scep.preprod.local.url", "preprodurl");
        System.setProperty("scep.preprod.local.username", "srvSCEP");
        System.setProperty("scep.preprod.local.password", "dilldall");
        System.setProperty("oem.url", "https://fjas.adeo.no");
        System.setProperty("oem.username", "eple");
        System.setProperty("oem.password", "banan");
        System.setProperty("bigip.url", "https://useriost.adeo.no");
        System.setProperty("bigip.username", "mango");
        System.setProperty("bigip.password", "chili");
        System.setProperty("mqadmin.u.username", "srvAura");
        System.setProperty("mqadmin.u.password", "bacon");
        System.setProperty("mqadmin.t.username", "srvAura");
        System.setProperty("mqadmin.t.password", "secret");
        System.setProperty("mqadmin.q.username", "srvAura");
        System.setProperty("mqadmin.q.password", "secret");
        System.setProperty("mqadmin.p.username", "srvAura");
        System.setProperty("mqadmin.p.password", "secret");

        System.setProperty("ldap.url", "ldap://ldapgw.test.local");
        System.setProperty("ldap.domain", "test.local");
    }

    public static void main(String[] args) throws Exception {
        BastaJettyRunner jetty = new BastaJettyRunner(8086, null);
        jetty.start();
        jetty.server.join();
    }

    public static File getProjectRoot() {
        File path = new File(BastaJettyRunner.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        while (!new File(path, "target").exists()) {
            path = path.getParentFile();
        }
        return path;
    }

    public static DataSource createDataSource(String type, String url, String username, String password) {
        System.setProperty("basta.db.type", type);
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMaxWait(20000);
        System.out.println("using database " + ds.getUsername() + "@" + ds.getUrl());
        return ds;
    }

    public WebAppContext getContext(String overrideDescriptor) {

        final WebAppContext context = new WebAppContext();
        context.setServer(server);
        context.setResourceBase(setupResourceBase());
        Configuration[] configurations = {new WebXmlConfiguration(), new WebInfConfiguration()};
        context.setConfigurations(configurations);
        if (overrideDescriptor != null) {
            context.setOverrideDescriptor(overrideDescriptor);
        }
        return context;
    }

    private String setupResourceBase() {
        try {
            File file = new File(getClass().getResource("/spring-security-unit-test.xml").toURI());
            File projectDirectory = file.getParentFile().getParentFile().getParentFile();
            File webappDir = new File(projectDirectory, WEB_SRC);
            return webappDir.getCanonicalPath();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("Could not find webapp directory", e);
        }
    }

    public WebApplicationContext getSpringContext() {
        WebAppContext webApp = (WebAppContext) server.getHandler();
        return WebApplicationContextUtils.getWebApplicationContext(webApp.getServletContext());
    }

    public void start() {
        try {
            server.start();
            System.out.println("Jetty started on port " + getPort());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void stop() {
        try {
            server.stop();
            System.out.println("Jetty stopped");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getPort() {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    public void setOrchestratorConfigProperties() {
        Properties orchestratorProperties = readEnfironmentSpecificPropertiesFrom("database.properties");

        System.setProperty("rest.orchestrator.provision.url", orchestratorProperties.getProperty("rest.orchestrator.provision.url"));
        System.setProperty("rest.orchestrator.decomission.url", orchestratorProperties.getProperty("rest.orchestrator.decomission.url"));
        System.setProperty("rest.orchestrator.startstop.url", orchestratorProperties.getProperty("rest.orchestrator.startstop.url"));
        System.setProperty("rest.orchestrator.modify.url", orchestratorProperties.getProperty("rest.orchestrator.modify.url"));
        System.setProperty("user.orchestrator.username", orchestratorProperties.getProperty("user.orchestrator.username"));
        System.setProperty("user.orchestrator.password", orchestratorProperties.getProperty("user.orchestrator.password"));
    }

    private Properties readEnfironmentSpecificPropertiesFrom(String filename) {
        Properties properties = new Properties();
        try {
            File propertyFile = new File(System.getProperty("user.home"), filename);
            if (!propertyFile.exists()) {
                throw new IllegalArgumentException("Propertyfile does not exist " + propertyFile.getAbsolutePath());
            }
            properties.load(new FileInputStream(propertyFile));
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    protected DataSource createDatasource() {
        Properties dbProperties = readEnfironmentSpecificPropertiesFrom("database.properties");

        return createDataSource(
                dbProperties.getProperty("basta.db.type"),
                dbProperties.getProperty("basta.db.url"),
                dbProperties.getProperty("basta.db.username"),
                dbProperties.getProperty("basta.db.password"));
    }
}
