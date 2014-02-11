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

    public BastaJettyRunner(int port, String overrideDescriptor) {
        server = new Server(port);
        setSystemProperties();
        WebAppContext context = getContext(overrideDescriptor);
        server.setHandler(context);

        // Add resources
        try {
            new Resource("java:/jdbc/bastaDB", createDatasourceFromPropertyfile());
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public WebAppContext getContext(String overrideDescriptor){

        final WebAppContext context = new WebAppContext();
        context.setServer(server);
        context.setResourceBase(setupResourceBase());
        Configuration[] configurations = {new WebXmlConfiguration(), new WebInfConfiguration()};
        context.setConfigurations(configurations);
        if (overrideDescriptor != null){
            context.setOverrideDescriptor(overrideDescriptor);
        }
        return context;
    }

    private String setupResourceBase() {
        try {
            File file = new File(getClass().getResource("/spring-security-unit-test.xml").toURI());
            File projectDirectory = file.getParentFile().getParentFile().getParentFile();
            File webappDir = new File(projectDirectory,  WEB_SRC);
            return webappDir.getCanonicalPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not find webapp directory", e);
        } catch (IOException e) {
            throw new RuntimeException("Could not find webapp directory", e);
        }
    }

    public WebApplicationContext getSpringContext() {
        WebAppContext webApp = (WebAppContext) server.getHandler();
        return WebApplicationContextUtils.getWebApplicationContext(webApp.getServletContext());
    }

    private void setSystemProperties() {
        System.setProperty("fasit.rest.api.url", "http://localhost:8088");
        System.setProperty("fasit.rest.api.username", "admin");
        System.setProperty("fasit.rest.api.password", "admin");
        System.setProperty("ldap.url", "ldap://ldapgw.adeo.no");
        System.setProperty("ldap.domain", "adeo.no");
        System.setProperty("ws.orchestrator.url", "https://a01drvw164.adeo.no:8281/vmware-vmo-webcontrol/webservice");
        System.setProperty("user.orchestrator.username", "srvOrchestrator@adeo.no");
        System.setProperty("user.orchestrator.password", "secret");
        // // TODO: This is just a temporary group in test local to verify that authentication and authorization works with a
        // real
        // // LDAP
        // System.setProperty("ROLE_OPERATIONS.groups", "(DG) Moderniseringsprogrammet Teknisk plattform");
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
        return ((ServerConnector)server.getConnectors()[0]).getLocalPort();
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        BastaJettyRunner jetty = new BastaJettyRunner(8086,null);
        jetty.start();
        jetty.server.join();
    }

    private static DataSource createDatasourceFromPropertyfile() {
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

}
