package no.nav.aura.basta.security;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class TrustStoreHelper {

    private static final Logger logger = LoggerFactory.getLogger(TrustStoreHelper.class);

    public static void configureTrustStore() {
        URL trustStoreURL;
        String trustStorePassword = "changeit";
        //if (System.getenv("NAV_TRUSTSTORE_PATH") != null || System.getenv("NAV_TRUSTSTORE_PASSWORD") != null) {
        //    try {
        //        File trustStoreFile = new File(System.getenv("NAV_TRUSTSTORE_PATH"));
        //        trustStoreURL = trustStoreFile.toURI().toURL();
        //    } catch (IOException e) {
        //        throw new RuntimeException("Unable to load truststore file from " + System.getenv("NAV_TRUSTSTORE_PATH"), e);
        //    }
        //    trustStorePassword = System.getenv("NAV_TRUSTSTORE_PASSWORD");
        //    logger.info("Using supplied truststore /etc/ssl/certs/java/cacerts");
        //} else {
        trustStoreURL = TrustStoreHelper.class.getClassLoader().getResource("truststore.jts");
        logger.info("Using bundled truststore file");
        //}

        File trustStoreTempFile = null;
        try {
            logger.debug("java.io.tmpdir = " + System.getProperty("java.io.tmpdir"));
            trustStoreTempFile = File.createTempFile("trustStore", ".jts");
            logger.info(String.format("Copying truststore file from %s to %s", trustStoreURL, trustStoreTempFile));
            FileUtils.copyURLToFile(Objects.requireNonNull(trustStoreURL), trustStoreTempFile);
            trustStoreTempFile.deleteOnExit();
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to copy truststore file to " + trustStoreTempFile, ioe);
        }
        logger.info(String.format("Setting system properties javax.net.ssl.trustStore=%s and javax.net.ssl.trustStorePassword=*************", trustStoreTempFile.getAbsolutePath()));
        System.setProperty("javax.net.ssl.trustStore", trustStoreTempFile.getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }
}
