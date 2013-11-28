package no.nav.aura.basta;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class JettyRunner {

    private static final String WEB_SRC = "src/main/webapp";
    private Server server;

    public JettyRunner(int port) {
        server = new Server(port);

        setSystemProperties();

        WebAppContext webapp = new WebAppContext();
        webapp.setServer(server);
        webapp.setResourceBase(WEB_SRC);
        webapp.setConfigurations(new Configuration[] { new WebXmlConfiguration(), new WebInfConfiguration() });
        server.setHandler(webapp);
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
        System.setProperty("ws.orchestrator.url", "https://a01drvw165.adeo.no:8281/vmware-vmo-webcontrol/webservice");
        System.setProperty("user.orchestrator.username", "srvOrchestrator@adeo.no");
        System.setProperty("user.orchestrator.password", "secret");
        // TODO: This is just a temporary group in test local to verify that authentication and authorization works with a real
        // LDAP
        System.setProperty("ROLE_OPERATIONS.groups", "(DG) Moderniseringsprogrammet Teknisk plattform");
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
        return server.getConnectors()[0].getLocalPort();
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JettyRunner jetty = new JettyRunner(8086);
        jetty.start();
        jetty.server.join();
    }

}
