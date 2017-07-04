package no.nav.aura.basta.rest.api;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
