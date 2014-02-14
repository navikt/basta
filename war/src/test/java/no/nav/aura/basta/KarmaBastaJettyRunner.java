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

public class KarmaBastaJettyRunner extends BastaJettyRunner {


    public KarmaBastaJettyRunner(int port, String overrideDescriptor) {

        super(port, overrideDescriptor);
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        KarmaBastaJettyRunner jetty = new KarmaBastaJettyRunner(1337, "war/src/test/resources/override-web.xml");
        jetty.start();
        jetty.server.join();
    }


}
