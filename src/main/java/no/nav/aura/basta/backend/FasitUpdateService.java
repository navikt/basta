package no.nav.aura.basta.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.aura.basta.backend.fasit.payload.LifeCycleStatus;
import no.nav.aura.basta.backend.fasit.rest.model.LifecyclePayload;
import no.nav.aura.basta.backend.fasit.rest.model.NodePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.SecretPayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.vm.Converters;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.security.User;
import no.nav.aura.basta.util.StatusLogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class FasitUpdateService {

    private static final Logger log = LoggerFactory.getLogger(FasitUpdateService.class);

    private RestClient restClient;

    @Value("${fasit_base_url}")
    private String fasitBaseURL;

    @Inject
	public FasitUpdateService(RestClient restClient) {
        this.restClient = restClient;
    }
    
    public static NodePayload createNodePayload(OrchestratorNodeDO vm, VMOrderInput input) {
		NodePayload nodePayload = new NodePayload(
				vm.getHostName(),
				input.getEnvironmentClass(),
				input.getEnvironmentName(),
				Converters.fasitPlatformTypeEnumFrom(input.getNodeType())
				);
		
		nodePayload.username = vm.getDeployUser();
		
		nodePayload.password = SecretPayload.withValue(vm.getDeployerPassword());
		Set<String> appSet = new HashSet<>();
		appSet.add(input.getApplicationMappingName());
		nodePayload.setApplications(appSet);
		
		nodePayload.zone = input.getZone();
		return nodePayload;
	}

    private void logError(Order order, String message, RuntimeException e) {
        order.addStatuslogWarning(message + " " + StatusLogHelper.abbreviateExceptionMessage(e));
        log.error("Error updating Fasit with order " + order.getId(), e);
    }

    public Optional<ResourcePayload> getResource(long fasitId) {
        try {
        	return restClient.getFasitResourceById(fasitId);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    public void createWASDeploymentManagerResource(OrchestratorNodeDO vm, VMOrderInput input, String resourceName, Order order) {
    	
    	ScopePayload scope = new ScopePayload();
    	scope.environmentClass(input.getEnvironmentClass());
    	scope.environment(input.getEnvironmentName());
    	scope.zone(input.getZone());
    	
    	
    	Map<String, String> properties = new HashMap<>();
    	properties.put("hostname", vm.getHostName());
    	properties.put("username", vm.getDeployUser());
    	
    	
    	SecretPayload passwordSecret = SecretPayload.withValue( vm.getDeployerPassword());
    	
    	Map<String, SecretPayload> secrets = new HashMap<>();
    	secrets.put("password", passwordSecret);
    	
    	
    	ResourcePayload resourcePayload = new ResourcePayload(
    			ResourceType.DeploymentManager,
    			resourceName,
    			scope,
    			properties,
    			secrets,
    			null, null, null, null, false);
    	
    	log.info("Creating WAS Deployment Manager resource using v2 api {} ", toJson(resourcePayload), fasitBaseURL + "/api/v2/resources");
    	restClient.createFasitResource(
				fasitBaseURL + "/api/v2/resources",
				toJson(resourcePayload),
				order.getCreatedBy(),
				"Bestilt i Basta med jobb " + order.getId() + " av " + order.getCreatedBy());
    }

    public Optional<String> registerNode(NodePayload nodePayload, Order order) {
		log.info("Creating node using v2 api {} ", nodePayload.hostname, fasitBaseURL + "/api/v2/nodes");
		order.addStatuslogInfo("Updating Fasit with node " + nodePayload.hostname);
		try {
			final String comment = "Bestilt i Basta med jobb " + order.getId() + " av " + order.getCreatedBy();
			Optional<String> createdNodeId = restClient.createFasitResource(
					fasitBaseURL + "/api/v2/nodes",
					toJson(nodePayload),
					order.getCreatedBy(),
					comment);

			final String message = "Successfully created Fasit node " + nodePayload.hostname;
			order.addStatuslogSuccess(message);
			log.info(message);
			return createdNodeId;
		} catch (RuntimeException e) {
			logError(order, "Updating Fasit with node " + nodePayload.hostname + " failed ", e);
			return Optional.empty();
		}
    }

    public void removeFasitEntity(final Order order, String hostname) {
        try {
        	String onBehalfOf = User.getCurrentUser().getName();
            String comment = "Slettet " + hostname + " i Basta av " + order.getCreatedBy();
            String url = fasitBaseURL + "/api/v2/nodes/" + hostname;
            restClient.deleteFasitResource(url, onBehalfOf, comment);
            log.info("Delete fasit entity for host " + hostname);
            order.addStatuslogInfo("Removed Fasit entity for host " + hostname);
        } catch (IllegalArgumentException e) {
            log.info("Node " + hostname + " not found in Fasit");
            order.addStatuslogInfo("No fasit entity for host " + hostname);
        } catch (Exception e) {
            log.error("Could not remove fasit entity for host " + hostname, e);
            order.addStatuslogWarning("Could not remove fasit entity for host " + hostname);
        }

    }

    public void startFasitEntity(Order order, String hostname) {
        try {
    		String onBehalfOf = User.getCurrentUser().getName();
            String url = fasitBaseURL + "/api/v2/nodes/" + hostname;
            
            
            LifecyclePayload lifecyclePayload = new LifecyclePayload();
            lifecyclePayload.status = LifeCycleStatus.RUNNING;
            
            NodePayload nodePayload = new NodePayload();
            nodePayload.lifecycle = lifecyclePayload;
            
            restClient.updateFasitResource(url, toJson(nodePayload), onBehalfOf, "Startet " + hostname + " i Basta av " + order.getCreatedBy());
            log.info("Started fasit entity for host " + hostname);
            order.addStatuslogInfo("Started Fasit entity for host " + hostname);
        } catch (Exception e) {
            log.error("Starting fasit entity for host " + hostname + " failed", e);
            order.addStatuslogWarning("Starting Fasit entity for host " + hostname + " failed");
        }
    }

    public void stopFasitEntity(Order order, String hostname) {
        try {
    		String onBehalfOf = User.getCurrentUser().getName();
        	String url = fasitBaseURL + "/api/v2/nodes/" + hostname;

            LifecyclePayload lifecyclePayload = new LifecyclePayload();
            lifecyclePayload.status = LifeCycleStatus.STOPPED;
            
            NodePayload nodePayload = new NodePayload();
            nodePayload.setHostname(hostname);
            nodePayload.lifecycle = lifecyclePayload;
            
            restClient.updateFasitResource(url, toJson(nodePayload), onBehalfOf, "Stoppet " + hostname + " i Basta av " + order.getCreatedBy());
            log.info("Stopped fasit entity for host " + hostname);
            order.addStatuslogInfo("Stopped Fasit entity for host " + hostname);
        } catch (Exception e) {
            log.error("Stopping fasit entity for host " + hostname + " failed", e);
            order.addStatuslogWarning("Stopping Fasit entity for host " + hostname + " failed");
        }
    }

    public Optional<String> createOrUpdateResource(Long id, ResourcePayload resource, Order order) {
        final String comment = "Bestilt i Basta med jobb " + order.getId() + " av " + order.getCreatedBy();
        Optional<String> orderId;

        try {
            if (id == null) {
                log.debug("Created resource in Fasit with alias {}", resource.alias);
                orderId = restClient.createFasitResource(fasitResourcesUrl(), toJson(resource), order.getCreatedBy(), comment);
            } else {
                log.debug("Updating resource in Fasit with id {}", id);
                final String updateUrl = fasitResourcesUrl() + "/" + id;
                restClient.updateFasitResource(updateUrl, toJson(resource), order.getCreatedBy(), comment);
                orderId = Optional.of(id.toString());
            }

            final String message = "Successfully created Fasit resource " + resource.alias + " (" + resource.type.name() + ")";
            order.addStatuslogSuccess(message);
            log.info(message);

            return orderId;
        } catch (RuntimeException e) {
            logError(order, "Creating Fasit resource failed", e);
            return Optional.empty();
        }
    }

    public Optional<String> createResource(ResourcePayload resource, Order order) {
        log.info("Creating resource using v2 api {} ", resource.type, fasitResourcesUrl());
        try {
            final String comment = "Bestilt i Basta med jobb " + order.getId() + " av " + order.getCreatedBy();
            Optional<String> createdResourceId = restClient.createFasitResource(
                    fasitResourcesUrl(),
                    toJson(resource),
                    order.getCreatedBy(),
                    comment);

            final String message = "Successfully created Fasit resource " + resource.alias + " (" + resource.type.name() + ")";
            order.addStatuslogSuccess(message);
            log.info(message);
            return createdResourceId;
        } catch (RuntimeException e) {
            logError(order, "Creating Fasit resource failed", e);
            return Optional.empty();
        }
    }
    
    public void setLifeCycleStatus(ResourcePayload resource, LifeCycleStatus state, Order order){
        String logMessage = "Updating resource " + resource.alias + "(" + resource.id + ") in fasit to " + state;
        order.getStatusLogs().add(new OrderStatusLog(resource.type.name(), logMessage, "fasit" ));
        String url = fasitBaseURL + "/api/v1/lifecycle/" +"Resource/" + order.getId();
        String comment = resource.type + " is updated to " + state + " from Basta by order " + order.getId();
        LifecyclePayload lifecyclePayload = new LifecyclePayload();
        lifecyclePayload.status = state;
        restClient.updateFasitResource(url, toJson(lifecyclePayload), User.getCurrentUser().getName(), comment);

    }

    public boolean deleteResource(Long id, String comment, Order order) {
        final String url = fasitResourcesUrl() + "/" + id;
        ResponseEntity<String> fasitResponse = restClient.deleteFasitResource(url, User.getCurrentUser().getName(), comment);


        if (fasitResponse.getStatusCode() == HttpStatus.NO_CONTENT) {
            order.addStatuslogSuccess("Successfully deleted resource with id " + id + " from fasit");
            return true;
        } else {
            final String message = "Unable to delete resource with id " + id + " from fasit. Got HTTPresponse " + fasitResponse.getStatusCode();
            log.error(message);
            order.addStatuslogWarning(message);
            return false;
        }
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