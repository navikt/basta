package no.nav.aura.basta.rest.serviceuser;

import static no.nav.aura.envconfig.client.ResourceTypeDO.Certificate;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.backend.serviceuser.ServiceUserAccount;
import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.backend.serviceuser.cservice.GeneratedCertificate;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.domain.input.serviceuser.ServiceUserOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.result.serviceuser.ServiceUserResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.security.User;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.DomainDO.EnvClass;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("/orders/serviceuser/certificate")
@Transactional
public class ServiceUserCertificateRestService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceUserCertificateRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private FasitRestClient fasit;

    @Inject
    private CertificateService certificateService;


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOrUpdateCertificate(Map<String, String> map, @Context UriInfo uriInfo) {

        ServiceUserOrderInput input = new ServiceUserOrderInput(map);
        input.setResultType(Certificate);

        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        Order order = new Order(OrderType.ServiceUser, OrderOperation.CREATE, input);
        logger.info("Create certificate order {} with input {}", order.getId(), map);
        order.setExternalId("N/A");
        ServiceUserAccount userAccount = input.getUserAccount();

        order.getStatusLogs().add(new OrderStatusLog("Certificate", "Creating new sertificate for " + userAccount.getUserAccountName() + " in " + userAccount.getDomainFqdn(), "cert", StatusLogLevel.success));
        GeneratedCertificate certificate = certificateService.createServiceUserCertificate(userAccount);
        order.getStatusLogs().add(new OrderStatusLog("Certificate", "Certificate created", "cert"));
        ResourceElement resource = putCertificateInFasit(order, userAccount, certificate);
        ServiceUserResult result = order.getResultAs(ServiceUserResult.class);
        result.add(userAccount, resource);

        order.setStatus(OrderStatus.SUCCESS);
        order = orderRepository.save(order);

        return Response.created(UriFactory.createOrderUri(uriInfo, "getOrder", order.getId()))
                .entity("{\"id\":" + order.getId() + "}").build();
    }


    private ResourceElement putCertificateInFasit(Order order, ServiceUserAccount userAccount, GeneratedCertificate certificate) {
        ResourceElement resource = null;
        fasit.setOnBehalfOf(User.getCurrentUser().getName());
        if (existsInFasit(userAccount)) {
            ResourceElement fasitResource = getResource(userAccount, Certificate);
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Certificate exists in fasit with id " + fasitResource.getId(), "fasit"));
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Updating certificate in fasit", "fasit"));
            MultipartFormDataOutput data = createMultiPartCertificate(userAccount, certificate);

            resource = fasit.executeMultipart("POST", "resources/" + fasitResource.getId(), data, "Updated in Basta by " + User.getCurrentUser().getDisplayName(), ResourceElement.class);
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Certificate updated in fasit with alias " + resource.getAlias() + " id:" + resource.getId(), "fasit"));
        } else {
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Registering certificate in fasit", "fasit"));
            MultipartFormDataOutput data = createMultiPartCertificate(userAccount, certificate);
            resource = fasit.executeMultipart("PUT", "resources", data, "created in Basta by " + User.getCurrentUser().getDisplayName(), ResourceElement.class);
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Certificate registered in fasit with alias " + resource.getAlias() + " id:" + resource.getId(), "fasit"));
        }
        return resource;
    }

    private MultipartFormDataOutput createMultiPartCertificate(ServiceUserAccount userAccount, GeneratedCertificate certificate) {
        MultipartFormDataOutput data = new MultipartFormDataOutput();
        data.addFormData("alias", userAccount.getAlias(), MediaType.TEXT_PLAIN_TYPE);
        data.addFormData("scope.environmentclass", userAccount.getEnvironmentClass(), MediaType.TEXT_PLAIN_TYPE);
        data.addFormData("scope.domain", userAccount.getDomainFqdn(), MediaType.TEXT_PLAIN_TYPE);
        data.addFormData("scope.application", userAccount.getApplicationName(), MediaType.TEXT_PLAIN_TYPE);
        data.addFormData("type", ResourceTypeDO.Certificate, MediaType.TEXT_PLAIN_TYPE);

        data.addFormData("keystorealias", certificate.getKeyStoreAlias(), MediaType.TEXT_PLAIN_TYPE);
        data.addFormData("keystorepassword", certificate.getKeyStorePassword(), MediaType.TEXT_PLAIN_TYPE);
        data.addFormData("keystore.filename", certificate.generateKeystoreFileName(userAccount), MediaType.TEXT_PLAIN_TYPE);
        data.addFormData("keystore.file", getKeystoreAsByteArray(certificate), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        return data;
    }

    @GET
    @Path("existInFasit")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean existsInFasit(@QueryParam("application") String application, @QueryParam("environmentClass") EnvironmentClass envClass, @QueryParam("zone") Zone zone) {
        ServiceUserAccount serviceUserAccount = new ServiceUserAccount(envClass, zone, application);
        return existsInFasit(serviceUserAccount);
    }

    private boolean existsInFasit(ServiceUserAccount serviceUserAccount) {
        return fasit.resourceExists(EnvClass.valueOf(serviceUserAccount.getEnvironmentClass().name()), null, DomainDO.fromFqdn(serviceUserAccount.getDomainFqdn()), serviceUserAccount.getApplicationName(),
                Certificate, serviceUserAccount.getAlias());
    }

    private ResourceElement getResource(ServiceUserAccount serviceUserAccount, ResourceTypeDO type) {
        Collection<ResourceElement> resoruces = fasit.findResources(EnvClass.valueOf(serviceUserAccount.getEnvironmentClass().name()), null, DomainDO.fromFqdn(serviceUserAccount.getDomainFqdn()),
                serviceUserAccount.getApplicationName(),
                type, serviceUserAccount.getAlias());
        if (resoruces.size() != 1) {
            throw new RuntimeException("Found more than one or zero resources");
        }
        return resoruces.iterator().next();
    }

    private byte[] getKeystoreAsByteArray(GeneratedCertificate cert) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            cert.getKeyStore().store(out, cert.getKeyStorePassword().toCharArray());
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
