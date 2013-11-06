package no.nav.aura.bestillingsweb;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class JettyRunner {

    private static final String WEB_SRC = "www";
    private Server server;

    public JettyRunner(int port) {
        server = new Server(port);

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
        ServerConnector connector = (ServerConnector) server.getConnectors()[0];
        return connector.getLocalPort();
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
