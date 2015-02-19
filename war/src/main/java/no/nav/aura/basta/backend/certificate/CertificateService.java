package no.nav.aura.basta.backend.certificate;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.spi.BadRequestException;
import org.jscep.CertificateVerificationCallback;
import org.jscep.client.Client;
import org.jscep.transaction.EnrolmentTransaction;
import org.jscep.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Path("/api/certificate/{domain}/")
@Component
public class CertificateService {

    private static Logger log = LoggerFactory.getLogger(CertificateService.class);
    private PrivateKey privateKey;
    private X509Certificate clientCert;

    public CertificateService() {

        privateKey = getPrivateKey();
        clientCert = getCertificate();

        class CertificateServiceAuthenticator extends Authenticator {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                URL url = getRequestingURL();
                for (String domain : ApplicationConfig.getDomains()) {
                    ScepConnectionInfo connInfo = ApplicationConfig.getServerForDomain(domain);
                    if (url.toString().startsWith(connInfo.getServerURL())) {
                        log.info("Username for " + url.toString() + " is: " + connInfo.getUsername());
                        return new PasswordAuthentication(connInfo.getUsername(),
                                connInfo.getPassword().toCharArray());
                    }
                }
                log.info("No username found for URL: " + url.toString());
                return null;
            }
        }

        CertificateServiceAuthenticator authenticator = new CertificateServiceAuthenticator();
        Authenticator.setDefault(authenticator);
    }

    @Path("/test")
    @GET
    @Produces({ MediaType.TEXT_HTML })
    public String certInfo(@PathParam("domain") String domain) {
        StringBuilder output = new StringBuilder(ApplicationConfig.getHtmlHeader());
        output.append("<h1>Certificate server test for " + domain + "</h1>");
        Client client = initializeServerConnection(domain);
        output.append("<ul>");
        try {
            CertStore cs = client.getCaCertificate();
            Collection<? extends Certificate> certs = cs.getCertificates(null);
            for (Certificate cert : certs) {
                X509Certificate x509cert = (X509Certificate) cert;

                log.debug("CA Certificate: Issued To [" + x509cert.getIssuerX500Principal() +
                        "] Serial# [" + DatatypeConverter.printHexBinary(x509cert.getSerialNumber().toByteArray()) + "]");
                output.append("<li>CA Certificate: Issued To [" + x509cert.getIssuerX500Principal() +
                        "] Serial# [" + DatatypeConverter.printHexBinary(x509cert.getSerialNumber().toByteArray()) + "]</li>");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving CA cert", e);
        } catch (CertStoreException e) {
            throw new RuntimeException("Error retrieving CA Cert", e);
        }

        output.append("</ul>");
        output.append(ApplicationConfig.getHtmlFooter());
        return output.toString();
    }

    @GET
    @Produces({ MediaType.TEXT_HTML })
    public String certificateForm(@PathParam("domain") String domain) {
        StringBuilder output = new StringBuilder(ApplicationConfig.getHtmlHeader());
        ScepConnectionInfo connectionInfo = ApplicationConfig.getServerForDomain(domain);
        if (connectionInfo == null) {
            throw new BadRequestException("Unknown domain: " + domain);
        }

        output.append("<h1>Certificate server: " + domain + "</h1>");

        output.append("<form method=\"post\" enctype=\"multipart/form-data\">");

        output.append("<p>Certificate request file: ");
        output.append("<input type=\"file\" name=\"certificate\" />");
        output.append("</p>");
        output.append("<p><input type=\"submit\"></p>");
        output.append("</form>");

        output.append("</ul>");

        output.append("<p>Certificate server: " + connectionInfo.getServerURL() + "<br>");
        output.append("Username: " + connectionInfo.getUsername() + "<br>");
        output.append("Run <a href=\"test\">connectivity test</a></p>");
        output.append(ApplicationConfig.getHtmlFooter());
        return output.toString();
    }

    @POST
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.TEXT_PLAIN })
    public String signCertificateFromForm(MultipartFormDataInput fileData, @PathParam("domain") String domain) {
        try {
            return signCertificate(fileData.getFormDataPart("certificate", String.class, null), domain);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PUT
    @Consumes({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_OCTET_STREAM })
    @Produces({ MediaType.TEXT_PLAIN })
    public String signCertificate(String certificate, @PathParam("domain") String domain) {
        Client client = initializeServerConnection(domain);

        PKCS10CertificationRequest csr;

        try (PEMReader pr = new PEMReader(new StringReader(certificate))) {
            csr = (PKCS10CertificationRequest) pr.readObject();
        } catch (IOException e) {
            throw new RuntimeException("Unable to read certificate", e);
        }

        EnrolmentTransaction trans;
        Certificate cert;
        try {
            trans = client.enrol(csr);
            Transaction.State state = trans.send();
            while (state == Transaction.State.CERT_REQ_PENDING) {
                log.info("Waiting for signing operation to complete....");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // Do nothing, just loop around again if we're interrupted.
                }
                state = trans.poll();
            }
            if (state == Transaction.State.CERT_NON_EXISTANT) {
                throw new RuntimeException("Could not sign certificate: " + trans.getFailInfo());
            }
            CertStore store = trans.getCertStore();
            cert = store.getCertificates(null).iterator().next();
        } catch (IOException | CertStoreException e) {
            throw new RuntimeException("Could not sign certificate", e);
        }

        StringWriter stringWriter = new StringWriter();
        try (PEMWriter wr = new PEMWriter(stringWriter)) {
            wr.writeObject(cert);
        } catch (IOException e) {
            throw new RuntimeException("Could not write certificate", e);
        }

        return stringWriter.toString();

    }

    private Client initializeServerConnection(String domain) {
        ScepConnectionInfo connectionInfo = ApplicationConfig.getServerForDomain(domain);
        if (connectionInfo == null) {
            throw new BadRequestException("Unknown domain: " + domain);
        }

        String scepServerURL = connectionInfo.getServerURL();

        log.info("Connecting to: " + scepServerURL);

        URL serverURL;
        try {
            URL u = new URL(scepServerURL);
            serverURL = u;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid server URL: " + scepServerURL, e);
        }

        CallbackHandler handler = new CallbackHandler() {

            @Override
            public void handle(Callback[] callbacks) throws IOException,
                    UnsupportedCallbackException {
                for (Callback c : callbacks) {
                    if (c instanceof CertificateVerificationCallback) {
                        CertificateVerificationCallback cvc = (CertificateVerificationCallback) c;
                        cvc.setVerified(true);
                        continue;
                    }
                    log.error("Unsupported callback: " + c.toString());
                    throw new UnsupportedCallbackException(c);
                }
            }

        };

        // The last parameter to the Client constructor is necessary to get the MSCEP service to return data
        Client client = new Client(serverURL, clientCert, privateKey, handler, "nav-certificate-service");

        return client;
    }

    private X509Certificate getCertificate() {
        X509Certificate clientCert;
        try (InputStream certificateInputStream = getClass().getResourceAsStream("/certificate/scep-client.crt")) {
            clientCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(certificateInputStream);
        } catch (CertificateException | IOException e) {
            throw new RuntimeException("Could not read certificate", e);
        }
        return clientCert;
    }

    private PrivateKey getPrivateKey() {
        try (InputStream is = getClass().getResourceAsStream("/certificate/scep-client.pkcs8")) {
            byte[] tmp = new byte[4096];
            int size = 0;
            while (true) {
                if (tmp.length - size <= 0) {
                    throw new IOException("File is too big. Maximum file size for the key file is " + tmp.length + " bytes");
                }
                int nread = is.read(tmp, size, tmp.length - size);
                if (nread == -1)
                    break;
                size += nread;
            }
            byte[] buf = new byte[size];
            System.arraycopy(tmp, 0, buf, 0, size);

            KeySpec keySpec = new PKCS8EncodedKeySpec(buf);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Could not read key", e);
        }
    }
}
