package no.nav.aura.basta.rest.api;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.security.Guard;

@Path("/api/certificate/{domain}/")
@Component
public class CertificateRestApi {

    private static Logger log = LoggerFactory.getLogger(CertificateRestApi.class);

    @Inject
    private CertificateService certificateService;

    public CertificateRestApi() {
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
    public String signCertificate(String certificate, @PathParam("domain") String domainString) {
        log.info("Processing certificate request for {}", domainString);
        Domain domain = Domain.fromFqdn(domainString);
        Guard.checkAccessToEnvironmentClass(domain.getEnvironmentClass());
        return certificateService.signCertificate(certificate, domain);

    }
}
