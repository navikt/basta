package no.nav.aura.basta.backend.serviceuser.cservice;

import no.nav.aura.basta.backend.serviceuser.PasswordGenerator;
import no.nav.aura.basta.backend.serviceuser.SecurityConfigElement;
import no.nav.aura.basta.backend.serviceuser.SecurityConfiguration;
import no.nav.aura.basta.backend.serviceuser.ServiceUserAccount;
import no.nav.aura.basta.domain.input.Domain;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.jboss.resteasy.spi.BadRequestException;
import org.jscep.client.CertificateVerificationCallback;
import org.jscep.client.Client;
import org.jscep.client.ClientException;
import org.jscep.client.EnrollmentResponse;
import org.jscep.transaction.TransactionException;
import org.jscep.transaction.TransactionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.x500.X500Principal;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

public class CertificateService {

	private Logger log = LoggerFactory.getLogger(CertificateService.class);
	private final static String SIG_ALG = "MD5WithRSA";
	private final static String keyStoreAlias = "app-key";
	private final static String scepProfile = "nav-certificate-service";

	private PrivateKey privateKey;
	private X509Certificate clientCert;
	private X500Principal principal;
	private SecurityConfiguration securityConfig;

	public CertificateService() {
		this(new SecurityConfiguration());
	}

	public CertificateService(SecurityConfiguration configuration) {
		this.securityConfig = configuration;
		privateKey = getPrivateKey();
		clientCert = getCertificate();

		CertificateServiceAuthenticator authenticator = new CertificateServiceAuthenticator();
		Authenticator.setDefault(authenticator);
	}

	public GeneratedCertificate createServiceUserCertificate(ServiceUserAccount userAccount) {
		log.info("Create certificate for serviceuser {} in domain {}", userAccount.getUserAccountName(), userAccount.getDomain());
		try {
			GeneratedCertificate certificate = new GeneratedCertificate();
			KeyPair keyPair = generateKeyPair();
			principal = new X500Principal(userAccount.getServiceUserDN());
			StringBuffer csr = generatePEM(keyPair);
			X509Certificate derCert = generateCertificate(csr, userAccount);
			String keyStorePassword = PasswordGenerator.generate(10);
			KeyStore keyStore = generateJavaKeyStore(derCert, keyPair, keyStoreAlias, keyStorePassword);

			certificate.setKeyStore(keyStore);
			certificate.setKeyStoreAlias(keyStoreAlias);
			certificate.setKeyStorePassword(keyStorePassword);
			return certificate;
		} catch (Exception e) {
			log.error("Unable to create certificate ", e);
			throw new RuntimeException(e);
		}
	}

	private StringBuffer generatePEM(KeyPair keyPair) throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		PKCS10CertificationRequest certreq = createCertificateRequest(keyPair);
		byte[] csr = certreq.getEncoded();

		StringBuffer csrBuffer = new StringBuffer("");
		csrBuffer.append("-----BEGIN NEW CERTIFICATE REQUEST-----\n");
		csrBuffer.append(DatatypeConverter.printBase64Binary(csr));
		csrBuffer.append("\n");
		csrBuffer.append("-----END NEW CERTIFICATE REQUEST-----");

		return csrBuffer;
	}

	private PKCS10CertificationRequest createCertificateRequest(KeyPair keyPair) throws
			Exception {
		Security.addProvider(new BouncyCastleProvider());
		PKCS10CertificationRequestBuilder crBuilder = new JcaPKCS10CertificationRequestBuilder(principal,
				keyPair.getPublic());
		JcaContentSignerBuilder csrSignerBuilder = new JcaContentSignerBuilder(SIG_ALG);
		ContentSigner contentSigner = csrSignerBuilder.build(keyPair.getPrivate());
		PKCS10CertificationRequest certificationRequest = crBuilder.build(contentSigner);

		return certificationRequest;
	}

	private KeyPair generateKeyPair() throws Exception {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);
		KeyPair keyPair = keyGen.generateKeyPair();

		return keyPair;
	}

	private X509Certificate generateCertificate(StringBuffer csr, ServiceUserAccount userAccount) throws Exception {
		String pemFile = signCertificate(csr.toString(), userAccount.getDomain());

		String base64 = pemFile.replaceAll("\\s", "");
		base64 = base64.replace("-----BEGINCERTIFICATE-----", "");
		base64 = base64.replace("-----ENDCERTIFICATE-----", "");

		byte[] derFile = org.bouncycastle.util.encoders.Base64.decode(base64.getBytes(StandardCharsets.UTF_8));

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

	public String signCertificate(String certificate, Domain domain) {
		Client client = initializeServerConnection(domain);

		PKCS10CertificationRequest csr;

		try (PEMParser pr = new PEMParser(new StringReader(certificate))) {
			csr = (PKCS10CertificationRequest) pr.readObject();
		} catch (IOException e) {
			throw new RuntimeException("Unable to read certificate", e);
		}

		EnrollmentResponse response;
		Certificate cert;
		if (principal == null) { principal = new X500Principal(""); }

		try {
			response = client.enrol(clientCert, privateKey, csr, scepProfile);
			while (response.isPending()) {
				log.info("Waiting for signing operation to complete....");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// Do nothing, just loop around again if we're interrupted.
				}
				TransactionId transactionId = response.getTransactionId();

				response = client.poll(clientCert, privateKey, principal, transactionId, scepProfile);
			}

			if (response.isFailure()) {
				throw new RuntimeException("Could not sign certificate: " + response.getFailInfo());
			}

			CertStore store = response.getCertStore();
			cert = store.getCertificates(null).iterator().next();
		} catch (CertStoreException | TransactionException | ClientException e) {
			throw new RuntimeException("Could not sign certificate", e);
		}

		StringWriter stringWriter = new StringWriter();
		try (JcaPEMWriter wr = new JcaPEMWriter(stringWriter)) {
			wr.writeObject(cert);
		} catch (IOException e) {
			throw new RuntimeException("Could not write certificate", e);
		}

		return stringWriter.toString();

	}

	private Client initializeServerConnection(Domain domain) {

		SecurityConfigElement connectionInfo = securityConfig.getConfigForDomain(domain);
		if (connectionInfo == null) {
			throw new BadRequestException("Unknown domain: " + domain);
		}

        URL serverURL;
        URI scepServerURL = connectionInfo.getSigningURL();
        log.info("Connecting to CA server: {} for {}", scepServerURL, domain);
        try {
            serverURL = scepServerURL.toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid server URL: " + scepServerURL, e);
        }

		CallbackHandler handler = callbacks -> {
			for (Callback c : callbacks) {
				if (c instanceof CertificateVerificationCallback) {
					CertificateVerificationCallback cvc = (CertificateVerificationCallback) c;
					cvc.setVerified(true);
					continue;
				}
				log.error("Unsupported callback: " + c.toString());
				throw new UnsupportedCallbackException(c);
			}
		};

		// The last parameter to the Client constructor is necessary to get the
		// MSCEP service to return data , "nav-certificate-service"
        Client client = new Client(serverURL, handler);

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
