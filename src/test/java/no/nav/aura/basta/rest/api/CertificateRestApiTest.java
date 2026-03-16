package no.nav.aura.basta.rest.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import jakarta.inject.Inject;

import no.nav.aura.basta.ApplicationTest;
import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.domain.input.Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class CertificateRestApiTest extends ApplicationTest {

    // Fake PEM certificate returned by the mocked CertificateService
    private static final String FAKE_SIGNED_CERT =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIICpDCCAYwCCQDU0+xGcHNGqDANBgkqhkiG9w0BAQsFADAUMRIwEAYDVQQDDAls\n" +
            "b2NhbGhvc3QwHhcNMjMwMTAxMDAwMDAwWhcNMjQwMTAxMDAwMDAwWjAUMRIwEAYD\n" +
            "VQQDDAlsb2NhbGhvc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC7\n" +
            "-----END CERTIFICATE-----\n";

    @Inject
    private CertificateService certificateService;

    private byte[] csrBytes;
    private File csrFile;

    @BeforeEach
    public void loadCsr() throws IOException, URISyntaxException {
        csrFile = Paths.get(getClass().getResource("/no/nav/aura/basta/hostname.csr").toURI()).toFile();
        csrBytes = Files.readAllBytes(csrFile.toPath());

        // Stub signCertificate to avoid real SCEP calls
        when(certificateService.signCertificate(any(String.class), any(Domain.class)))
                .thenReturn(FAKE_SIGNED_CERT);
    }

    @Test
    public void signCertificateHappyPath() {
        given()
                .auth().preemptive().basic("prodadmin", "prodadmin")
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .body(csrBytes)
                .expect()
                .log().ifError()
                .statusCode(200)
                .body(notNullValue())
                .body(containsString("BEGIN CERTIFICATE"))
                .when()
                .put("/rest/api/certificate/preprod.local");
    }

    @Test
    public void signCertificateWithOctetStreamContentType() {
        given()
                .auth().preemptive().basic("prodadmin", "prodadmin")
                .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(csrBytes)
                .expect()
                .log().ifError()
                .statusCode(200)
                .body(containsString("BEGIN CERTIFICATE"))
                .when()
                .put("/rest/api/certificate/preprod.local");
    }

    @Test
    public void signCertificateWithoutAuthFails() {
        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .body(csrBytes)
                .expect()
                .statusCode(401)
                .when()
                .put("/rest/api/certificate/preprod.local");
    }

    @Test
    public void signCertificateWithUnknownDomainFails() {
        given()
                .auth().preemptive().basic("prodadmin", "prodadmin")
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .body(csrBytes)
                .expect()
                .log().ifError()
                .statusCode(400)
                .body(containsString("unknown.domain"))
                .when()
                .put("/rest/api/certificate/unknown.domain");
    }

    @Test
    public void signCertificateWithUserLackingAccessFails() {
        // "operation" user only has OPERATIONS role, which covers t/q – but let's
        // use a plain user (ROLE_USER only) who cannot access preprod.local (EnvironmentClass q)
        given()
                .auth().preemptive().basic("user", "user")
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .body(csrBytes)
                .expect()
                .statusCode(403)
                .when()
                .put("/rest/api/certificate/preprod.local");
    }

    // --- @PostMapping (multipart/form-data) tests ---

    @Test
    public void signCertificateFromFormHappyPath() {
        given()
                .auth().preemptive().basic("prodadmin", "prodadmin")
                .multiPart("certificate", csrFile, MediaType.TEXT_PLAIN_VALUE)
                .expect()
                .log().ifError()
                .statusCode(200)
                .body(containsString("BEGIN CERTIFICATE"))
                .when()
                .post("/rest/api/certificate/preprod.local");
    }

    @Test
    public void signCertificateFromFormWithoutAuthFails() {
        given()
                .multiPart("certificate", csrFile, MediaType.TEXT_PLAIN_VALUE)
                .expect()
                .statusCode(401)
                .when()
                .post("/rest/api/certificate/preprod.local");
    }

    @Test
    public void signCertificateFromFormWithUnknownDomainFails() {
        // Domain.fromFqdn throws IllegalArgumentException, mapped to 400 by GlobalExceptionHandler
        given()
                .auth().preemptive().basic("prodadmin", "prodadmin")
                .multiPart("certificate", csrFile, MediaType.TEXT_PLAIN_VALUE)
                .expect()
                .statusCode(400)
                .body(containsString("unknown.domain"))
                .when()
                .post("/rest/api/certificate/unknown.domain");
    }

    @Test
    public void signCertificateFromFormWithUserLackingAccessFails() {
        given()
                .auth().preemptive().basic("user", "user")
                .multiPart("certificate", csrFile, MediaType.TEXT_PLAIN_VALUE)
                .expect()
                .statusCode(403)
                .when()
                .post("/rest/api/certificate/preprod.local");
    }
}
