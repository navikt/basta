package no.nav.aura.basta.rest.serviceuser;

import static no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType.Certificate;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;
import no.nav.aura.basta.backend.FasitRestClient;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload.FilePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.SecretPayload;
import no.nav.aura.basta.backend.fasit.rest.model.infrastructure.Zone;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.backend.serviceuser.FasitServiceUserAccount;
import no.nav.aura.basta.backend.serviceuser.cservice.CertificateService;
import no.nav.aura.basta.backend.serviceuser.cservice.GeneratedCertificate;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.serviceuser.ServiceUserOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.result.serviceuser.ServiceUserResult;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.security.User;

@Component
@RestController
@RequestMapping("/rest/orders/serviceuser/certificate")
@Transactional
public class ServiceUserCertificateRestService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceUserCertificateRestService.class);

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private FasitRestClient fasitRestClient;

    @Inject
    private CertificateService certificateService;
    
    @Value("${fasit_base_url}")
    private String fasitBaseURL;

    @PostMapping
    public ResponseEntity<?> createOrUpdateCertificate(@RequestBody Map<String, String> map) {

        ServiceUserOrderInput input = new ServiceUserOrderInput(map);
        input.setResultType(Certificate);

        Guard.checkAccessToEnvironmentClass(input.getEnvironmentClass());

        Order order = new Order(OrderType.ServiceUser, OrderOperation.CREATE, input);
        logger.info("Create certificate order {} with input {}", order.getId(), map);
        order.setExternalId("N/A");
        FasitServiceUserAccount userAccount = input.getUserAccount();

        order.getStatusLogs().add(new OrderStatusLog("Certificate", "Creating new sertificate for " + userAccount.getUserAccountName() + " in " + userAccount.getDomainFqdn(), "cert", StatusLogLevel.success));
        GeneratedCertificate certificate = certificateService.createServiceUserCertificate(userAccount);
        order.getStatusLogs().add(new OrderStatusLog("Certificate", "Certificate created", "cert"));
        ResourcePayload resource = putCertificateInFasit(order, userAccount, certificate);
        ServiceUserResult result = order.getResultAs(ServiceUserResult.class);
        result.add(userAccount, resource);

        order.setStatus(OrderStatus.SUCCESS);
        order = orderRepository.save(order);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/rest/orders/{id}")
                .buildAndExpand(order.getId())
                .toUri();
        return ResponseEntity.created(location).body(Map.of("id", order.getId()));
    }


    private ResourcePayload putCertificateInFasit(Order order, FasitServiceUserAccount userAccount, GeneratedCertificate certificate) {
    	ResourcePayload resource = null;
//        fasit.setOnBehalfOf(User.getCurrentUser().getName());
    	
        if (existsInFasit(userAccount)) {
        	ResourcePayload fasitResource = getResource(userAccount, Certificate);
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Certificate exists in fasit with id " + fasitResource.id, "fasit"));
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Updating certificate in fasit", "fasit"));
            ResourcePayload payload = createCertificatePayload(userAccount, certificate);
            
            String comment = "Updated in Basta by " + User.getCurrentUser().getDisplayName();
            final String url = fasitResourcesUrl() + "/" + fasitResource.id;
            
            resource = fasitRestClient.updateFasitResourceAndReturnResourcePayload(url, toJson(payload), null, comment);
            
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Certificate updated in fasit with alias " + resource.getAlias() + " id:" + resource.id, "fasit"));
        } else {
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Registering certificate in fasit", "fasit"));
//            MultipartFormDataOutput data = createMultiPartCertificate(userAccount, certificate);
            ResourcePayload payload = createCertificatePayload(userAccount, certificate);
//            resource = fasit.executeMultipart("PUT", "resources", data, "created in Basta by " + User.getCurrentUser().getDisplayName(), ResourceElement.class);
            String comment = "Created in Basta by " + User.getCurrentUser().getDisplayName();
            Optional<String> resourceid = fasitRestClient.createFasitResource(fasitResourcesUrl(), toJson(payload), null, comment);
            resource = fasitRestClient.getFasitResourceById(Long.valueOf(resourceid.get()))
            					 .orElseThrow(() -> new RuntimeException("Could not fetch newly created resource from Fasit"));
            order.getStatusLogs().add(new OrderStatusLog("Fasit", "Certificate registered in fasit with alias " + resource.getAlias() + " id:" + resource.id, "fasit"));
        }
        return resource;
    }

    private ResourcePayload createCertificatePayload(FasitServiceUserAccount userAccount, GeneratedCertificate certificate) {
    	
    	SecretPayload secretPayload = SecretPayload.withValue(certificate.getKeyStorePassword());
    	
    	
		ResourcePayload payload = new ResourcePayload();
		payload.setType(ResourceType.Certificate);
		payload.setAlias(userAccount.getAlias());
		payload.scope = new ScopePayload()
				.environmentClass(userAccount.getEnvironmentClass())
				.application(userAccount.getApplicationName());

		payload.addProperty("keystorealias", certificate.getKeyStoreAlias());
		payload.secrets.put("keystorepassword", secretPayload);
		payload.files.put("keystore", new FilePayload(certificate.generateKeystoreFileName(userAccount), null, getKeystoreAsBase64String(certificate)));
		
		return payload;
	}

    @GetMapping("/existInFasit")
    public ResponseEntity<Boolean> existsInFasit(
            @RequestParam String application,
            @RequestParam EnvironmentClass environmentClass,
            @RequestParam Zone zone) {
        FasitServiceUserAccount serviceUserAccount = new FasitServiceUserAccount(environmentClass, zone, application);
        return ResponseEntity.ok(existsInFasit(serviceUserAccount));
    }

    private boolean existsInFasit(FasitServiceUserAccount serviceUserAccount) {
    	ScopePayload scope = new ScopePayload()
				.environmentClass(serviceUserAccount.getEnvironmentClass())
				.application(serviceUserAccount.getApplicationName());
    	
    	return fasitRestClient.existsInFasit(ResourceType.Certificate, serviceUserAccount.getAlias(), scope);
    }

    private ResourcePayload getResource(FasitServiceUserAccount serviceUserAccount, ResourceType type) {
    	ScopePayload scope = new ScopePayload()
				.environmentClass(serviceUserAccount.getEnvironmentClass())
				.environment(null)
				.application(serviceUserAccount.getApplicationName())
				.zone(null);
        List<ResourcePayload> resources = fasitRestClient.findFasitResources(type, serviceUserAccount.getAlias(), scope);
				
        if (resources.isEmpty()) {
            throw new RuntimeException("Found more than one or zero resources");
        }
        return resources.get(0);
    }

    private String getKeystoreAsBase64String(GeneratedCertificate cert) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			cert.getKeyStore().store(out, cert.getKeyStorePassword().toCharArray());
			
			return Base64.getEncoder().encodeToString(out.toByteArray());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    public static String toJson(Object payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException("Error serializing payload to JSON", jpe);
        }
    }
    public String fasitResourcesUrl() {
		return fasitBaseURL + "/api/v2/resources";
	}
}
