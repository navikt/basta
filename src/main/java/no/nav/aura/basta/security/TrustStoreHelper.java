package no.nav.aura.basta.security;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrustStoreHelper {

    private static Logger logger = LoggerFactory.getLogger(TrustStoreHelper.class);

    public static void configureTrustStoreWithProvidedTruststore() {
        configureTrustStore(null, null);
    }

    public static void configureTrustStore(String customTrustStoreFile, String customTrustStorePassword) {
        URL trustStoreFile;
        String trustStorePassword;
        trustStoreFile = TrustStoreHelper.class.getClassLoader().getResource("truststore.jts");
        trustStorePassword = "changeit";
        logger.info("Using default truststore bundled with the plugin");

        File trustStoreTempFile = null;
        try {
            logger.debug("java.io.tmpdir = " + System.getProperty("java.io.tmpdir"));
            trustStoreTempFile = File.createTempFile("trustStore", ".jts");
            logger.info(String.format("Copying truststore file from  %s to %s", trustStoreFile, trustStoreTempFile));
            FileUtils.copyURLToFile(trustStoreFile, trustStoreTempFile);
            trustStoreTempFile.deleteOnExit();
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to copy truststore file to " + trustStoreTempFile, ioe);
        }
        logger.info(String.format("Setting system properties javax.net.ssl.trustStore=%s and javax.net.ssl.trustStorePassword=*************", trustStoreTempFile.getAbsolutePath()));
        System.setProperty("javax.net.ssl.trustStore", trustStoreTempFile.getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }
}
