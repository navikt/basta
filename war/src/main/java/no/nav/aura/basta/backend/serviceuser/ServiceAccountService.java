package no.nav.aura.basta.backend.serviceuser;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;

import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class ServiceAccountService {

    private String domain = System.getProperty("domain");
    private String appName = System.getProperty("app");
    private String keyStoreAlias = "app-key";
    private String serverName = System.getProperty("server");
    private String certServiceUrl = "https://a01apvl048.adeo.no:8443/certificate/";
    private String certServiceUser = "admin";
    private String certServicePwd = "nimda";
    private String adAdminUser = "srvAura";
    private String adAdminPassword = System.getProperty("adPassword");
    private String envConfigRestUrl = "https://fasit.adeo.no/conf";
    private String envConfigUser = System.getProperty("envuser");
    private String envConfigPassword = System.getProperty("envpassword");

    private String serverSshUser = System.getProperty("serverSshUser");
    private String serverSshPassword = System.getProperty("serverSshPassword");

    private boolean createCertificate = Boolean.parseBoolean(System.getProperty("createCertificate", "true"));

    private Logger log = LoggerFactory.getLogger(ServiceAccountService.class);

    public ServiceAccountService() {
        checkUsernameAndPassword();
    }

    public void createServiceUser() {

        configureTrustStore();

        // if (serverName.length() == 0) {
        ServiceUserAccount userAccount = new ServiceUserAccount(appName, domain);
        EnvConfigService envConfig = new EnvConfigService(envConfigRestUrl, envConfigUser, envConfigPassword);

        ActiveDirectory ad = new ActiveDirectory(adAdminUser, adAdminPassword, userAccount);
        boolean adUserCreated = true;
        ad.create(userAccount);

        if (adUserCreated) {
            log.info("User {} created in AD {}. Adding Credentials to fasit", userAccount.getUserAccountName(),
                    userAccount.getDomain());
            envConfig.storeCredential(userAccount);
        }

        if (createCertificate && !envConfig.resourceExists(userAccount, "Certificate")) {
            log.info("User {} has no certificate in fasit for domain {}. Adding a new Certificate resource",
                    userAccount.getUserAccountName(), userAccount.getDomain());
            CertificateService certService = new CertificateService();
            certService.createServiceUserCertificate(userAccount, keyStoreAlias);
            envConfig.storeApplicationCertificate(userAccount);
        } else {
            log.info("User {} already has a certificate in fasit for domain {}", userAccount.getUserAccountName(),
                    userAccount.getDomain());
        }

        // } else {
        // ServiceUserAccount userAccount = new ServiceUserAccount("dummy", domain);
        // CertificateService certService = new CertificateService(certServiceUrl, certServiceUser, certServicePwd);
        // certService.createServerCert(userAccount, keyStoreAlias, serverName);
        // // certService.transferKeystore(serverName, serverSshUser, serverSshPassword, userAccount);
        // }

    }

    private static void configureTrustStore() {

        URL trustStoreFile = ServiceAccountService.class.getClassLoader().getResource("truststore.jts");
        String trustStorePassword = "cliTrustStore";

        File trustStoreTempFile = null;
        try {
            trustStoreTempFile = File.createTempFile("trustStore", ".jts");
            FileUtils.copyURLToFile(trustStoreFile, trustStoreTempFile);
            trustStoreTempFile.deleteOnExit();
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to copy truststore file to " + trustStoreTempFile.getAbsolutePath(), ioe);
        }
        System.setProperty("javax.net.ssl.trustStore", trustStoreTempFile.getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }

    private void checkUsernameAndPassword() {
        if (envConfigUser == null) {
            envConfigUser = System.getProperty("user.name");
            // getLog().info("No user defined. Setting user to current user: " + envConfigUser);
        }
        if (envConfigPassword == null) {
            // getLog().info("Enter password for user " + envConfigUser);
            char[] passwd = System.console().readPassword("[%s]", "Password for " + envConfigPassword + ":");
            envConfigPassword = new String(passwd);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        ServiceAccountService creator = new ServiceAccountService();
        creator.createServiceUser();
    }

    public void execute() {
        createServiceUser();
    }
}