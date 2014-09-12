package no.nav.aura.basta;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

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

public class BastaJettyRunner {

    private static final String WEB_SRC = "src/main/webapp";
    protected Server server;

    enum Env {
        U, TESTLOCAL
    }

    public BastaJettyRunner(int port, String overrideDescriptor) {
        server = new Server(port);
        setSystemProperties();
        setEnvironmentSpecificProperties(Env.U);
        WebAppContext context = getContext(overrideDescriptor);
        server.setHandler(context);

        // Add resources
        try {
            new Resource("java:/jdbc/bastaDB", createDatasource());
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    private void setEnvironmentSpecificProperties(Env environment) {
        switch (environment) {
        case TESTLOCAL:
            System.setProperty("fasit.rest.api.username", "");
            System.setProperty("fasit.rest.api.password", "");
            System.setProperty("ldap.url", "ldap://ldapgw.test.local");
            System.setProperty("ldap.domain", "test.local");
            break;
        case U:
        default:
            System.setProperty("fasit.rest.api.username", "admin");
            System.setProperty("fasit.rest.api.password", "admin");
            System.setProperty("ldap.url", "ldap://ldapgw.adeo.no");
            System.setProperty("ldap.domain", "adeo.no");
            break;
        }
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
        Configuration[] configurations = { new WebXmlConfiguration(), new WebInfConfiguration() };
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

    private void setSystemProperties() {
        System.setProperty("fasit.rest.api.url", "http://e34apsl00136.devillo.no:8080/conf");
        System.setProperty("ws.orchestrator.url", "https://a01drvw164.adeo.no:8281/vmware-vmo-webcontrol/webservice");
        System.setProperty("user.orchestrator.username", "srvOrchestrator@adeo.no");
        System.setProperty("user.orchestrator.password", "secret");
        System.setProperty("environment.class", "p");
        System.setProperty("ROLE_USER.groups", "0000-GA-STDAPPS");
        System.setProperty("ROLE_OPERATIONS.groups", "0000-GA-STDAPPS");
        // SUPERUSER ALL THE THINGS
        System.setProperty("ROLE_SUPERUSER.groups", "0000-GA-BASTA_SUPERUSER");
        System.setProperty("ROLE_PROD_OPERATIONS.groups", "0000-GA-DaTapoWeR_Logger_t");
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

    protected DataSource createDatasource() {
        Properties dbProperties = new Properties();
        try {
            File propertyFile = new File(System.getProperty("user.home"), "database.properties");
            if (!propertyFile.exists()) {
                throw new IllegalArgumentException("Propertyfile does not exist " + propertyFile.getAbsolutePath());
            }
            dbProperties.load(new FileInputStream(propertyFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return createDataSource(dbProperties.getProperty("basta.db.type"), dbProperties.getProperty("basta.db.url"), dbProperties.getProperty("basta.db.username"), dbProperties.getProperty("basta.db.password"));
    }

}
