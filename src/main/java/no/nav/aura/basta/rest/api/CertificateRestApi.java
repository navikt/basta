package no.nav.aura.basta.rest.api;

import java.io.IOException;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.security.Guard;

@Component
@RestController
@RequestMapping("/rest/api/certificate/{domain}")
public class CertificateRestApi {

    private static Logger log = LoggerFactory.getLogger(CertificateRestApi.class);

    @Inject
    private CertificateService certificateService;

    public CertificateRestApi() {
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> signCertificateFromForm(
            @RequestParam("certificate") MultipartFile fileData,
            @PathVariable String domain) {
        try {
            String certificateContent = new String(fileData.getBytes());
            return ResponseEntity.ok(signCertificateInternal(certificateContent, domain));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping(consumes = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE },
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> signCertificate(
            @RequestBody String certificate,
            @PathVariable String domain) {
    	String result;
		try {
			result = signCertificateInternal(certificate, domain);
			return ResponseEntity.ok(result);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} 
    }

    private String signCertificateInternal(String certificate, String domainString) {
        log.info("Processing certificate request for {}", domainString);
        Domain domain = Domain.fromFqdn(domainString);
        Guard.checkAccessToEnvironmentClass(domain.getEnvironmentClass());
        return certificateService.signCertificate(certificate, domain);
    }
}