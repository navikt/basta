package no.nav.aura.basta.backend.certificate.ad;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.RSAKeyGenParameterSpec;

import javax.security.auth.x500.X500Principal;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.DatatypeConverter;


import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import ch.qos.logback.classic.Logger;

public class CertificateService {

    private Logger log = LoggerFactory.getLogger(CertificateService.class);
    private String certServiceUrl;
    private String certServiceUser;
    private String certServicePwd;
    private final static String SIG_ALG = "MD5WithRSA";

    public CertificateService(String certServiceUrl, String certServiceUser, String certServicePwd) {
        this.certServiceUrl = certServiceUrl;
        this.certServiceUser = certServiceUser;
        this.certServicePwd = certServicePwd;
    }

    public KeyStore createCert(ServiceUserAccount userAccount, String keyStoreAlias) {
        try {
            KeyPair keyPair = generateKeyPair();
            StringBuffer csr = generatePEM(userAccount, SIG_ALG, keyPair);
            X509Certificate derCert = getCertificate(csr, certServiceUser, certServicePwd, certServiceUrl, userAccount);
            String keyStorePassword = PasswordGenerator.generate(10);
            KeyStore keyStore = generateJavaKeyStore(derCert, keyPair, keyStoreAlias, keyStorePassword);
            userAccount.setKeyStore(keyStore);
            userAccount.setKeyStoreAlias(keyStoreAlias);
            userAccount.setKeyStorePassword(keyStorePassword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public KeyStore createServerCert(ServiceUserAccount userAccount, String keyStoreAlias, String serverName) {
        try {
            KeyPair keyPair = generateKeyPair();
            StringBuffer csr = generateServerPEM(userAccount, SIG_ALG, keyPair, serverName);
            X509Certificate derCert = getCertificate(csr, certServiceUser, certServicePwd, certServiceUrl, userAccount);
            // String keyStorePassword = PasswordGenerator.generate(10);
            String keyStorePassword = userAccount.getDomain().split("\\.")[0] + "keystore1234";
            generateJavaKeyStoreFile(derCert, keyPair, keyStorePassword);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private StringBuffer generatePEM(ServiceUserAccount userAccount, String sigAlg, KeyPair keyPair) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        X500Principal principal = new X500Principal(userAccount.getUserFQDN());
        PKCS10CertificationRequest certreq = new PKCS10CertificationRequest(sigAlg, principal, keyPair.getPublic(), null, keyPair.getPrivate());
        byte[] csr = certreq.getEncoded();

        StringBuffer csrBuffer = new StringBuffer("");
        csrBuffer.append("-----BEGIN NEW CERTIFICATE REQUEST-----\n");
        csrBuffer.append(DatatypeConverter.printBase64Binary(csr));
        csrBuffer.append("\n");
        csrBuffer.append("-----END NEW CERTIFICATE REQUEST-----");

        return csrBuffer;
    }

    private StringBuffer generateServerPEM(ServiceUserAccount userAccount, String sigAlg, KeyPair keyPair, String serverName) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        X500Principal principal = new X500Principal(userAccount.getServerFQDN(serverName));
        PKCS10CertificationRequest certreq = new PKCS10CertificationRequest(sigAlg, principal, keyPair.getPublic(), null, keyPair.getPrivate());
        byte[] csr = certreq.getEncoded();

        StringBuffer csrBuffer = new StringBuffer("");
        csrBuffer.append("-----BEGIN NEW CERTIFICATE REQUEST-----\n");
        csrBuffer.append(DatatypeConverter.printBase64Binary(csr));
        csrBuffer.append("\n");
        csrBuffer.append("-----END NEW CERTIFICATE REQUEST-----");

        return csrBuffer;
    }

    private KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
        keyGen.initialize(spec);
        KeyPair keyPair = keyGen.generateKeyPair();

        return keyPair;
    }

    private X509Certificate getCertificate(StringBuffer csr, String certServiceUser, String certServicePwd, String certServiceUrl, ServiceUserAccount userAccount) throws Exception {
        log.info("Create and sign certificate");
        DefaultHttpClient httpClient = new DefaultHttpClient();
        Credentials credentials = new UsernamePasswordCredentials(certServiceUser, certServicePwd);
        httpClient.getCredentialsProvider().setCredentials(org.apache.http.auth.AuthScope.ANY, credentials);
        ClientExecutor clientExecutor = new ApacheHttpClient4Executor(httpClient);

        URI url = UriBuilder.fromPath(certServiceUrl).path(userAccount.getDomain()).build();
        log.debug("Calling {}", url);
        ClientRequest request = new ClientRequest(url.toString(), clientExecutor);
        request.body(MediaType.TEXT_PLAIN, csr);
        ClientResponse<String> response = request.put(String.class);
        if (response.getResponseStatus().getStatusCode() > 201) {
            throw new RuntimeException("Error code: " + response.getStatus() + ", " + response.getEntity(String.class));
        }

        String pemFile = response.getEntity();
        if (!userAccount.getApplicationName().isEmpty()) {
            writePemToFile(userAccount.getApplicationName() + "_" + userAccount.getDomain() + ".pem", pemFile);
        }

        String base64 = new String(pemFile).replaceAll("\\s", "");
        base64 = base64.replace("-----BEGINCERTIFICATE-----", "");
        base64 = base64.replace("-----ENDCERTIFICATE-----", "");

        byte[] derFile = org.bouncycastle.util.encoders.Base64.decode(base64.getBytes());

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(derFile));
        cert.checkValidity();
        return cert;
    }

    private KeyStore generateJavaKeyStore(X509Certificate derCert, KeyPair keyPair, String keyStoreAlias, String keyStorePassword) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, keyStorePassword.toCharArray());
        X509Certificate[] certChain = new X509Certificate[1];
        certChain[0] = derCert;
        ks.setKeyEntry(keyStoreAlias, keyPair.getPrivate(), keyStorePassword.toCharArray(), certChain);

        return ks;
    }

    private void generateJavaKeyStoreFile(X509Certificate derCert, KeyPair keyPair, String keyStorePassword) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, keyStorePassword.toCharArray());

        X509Certificate[] certChain = new X509Certificate[1];
        certChain[0] = derCert;
        ks.setKeyEntry("host-key", keyPair.getPrivate(), keyStorePassword.toCharArray(), certChain);

        FileOutputStream out = new FileOutputStream(new File("keystore.jks"));
        ks.store(out, keyStorePassword.toCharArray());
    }

    private void writePemToFile(String filename, String pem)
    {
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new FileWriter(filename));
            writer.write(pem);

        } catch (IOException e)
        {
        } finally
        {
            try
            {
                if (writer != null)
                    writer.close();
            } catch (IOException e)
            {
            }
        }
    }
}
