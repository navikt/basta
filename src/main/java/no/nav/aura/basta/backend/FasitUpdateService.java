package no.nav.aura.basta.backend;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import no.nav.aura.basta.backend.fasit.payload.LifeCycleStatus;
import no.nav.aura.basta.backend.fasit.payload.LifecyclePayload;
import no.nav.aura.basta.backend.fasit.payload.ResourcePayload;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.vm.Converters;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.security.User;
import no.nav.aura.basta.util.StatusLogHelper;
import no.nav.aura.envconfig.client.*;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static javax.ws.rs.core.Response.Status.NO_CONTENT;

@Component
public class FasitUpdateService {

    private static final Logger log = LoggerFactory.getLogger(FasitUpdateService.class);

    private FasitRestClient fasitRestClient;
    private RestClient fasitClient;

    @Value("${fasit_nodes_v2_url}")
    private String fasitNodeApi;

    @Value("${fasit_resources_v2_url}")
    private String fasitResourcesUrl;

    @Value("${fasit_lifecycle_v1_url}")
    private String fasitLifecycleApi;

    @Inject
    public FasitUpdateService(FasitRestClient fasitRestClient, RestClient restClient) {
        this.fasitRestClient = fasitRestClient;
        this.fasitClient = restClient;
    }

    public static NodeDO createNodeDO(OrchestratorNodeDO vm, VMOrderInput input) {
        NodeDO fasitNodeDO = new NodeDO();
        fasitNodeDO.setDomain(input.getDomain().getFqn());
        fasitNodeDO.setEnvironmentClass(input.getEnvironmentClass().name());
        fasitNodeDO.setEnvironmentName(input.getEnvironmentName());
        fasitNodeDO.setApplicationMappingName(input.getApplicationMappingName());
        fasitNodeDO.setZone(input.getZone().name());
        fasitNodeDO.setHostname(vm.getHostName());
        fasitNodeDO.setUsername(vm.getDeployUser());
        fasitNodeDO.setPassword(vm.getDeployerPassword());
        fasitNodeDO.setPlatformType(Converters.platformTypeDOFrom(input.getNodeType()));
        return fasitNodeDO;
    }

    private void logError(Order order, String message, RuntimeException e) {
        order.addStatuslogWarning(message + " " + StatusLogHelper.abbreviateExceptionMessage(e));
        log.error("Error updating Fasit with order " + order.getId(), e);
    }

    public ResourceElement getResource(long fasitId) {
        try {
            return fasitRestClient.getResourceById(fasitId);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    public void createWASDeploymentManagerResource(OrchestratorNodeDO vm, VMOrderInput input, String resourceName, Order order) {
        ResourceElement resource = new ResourceElement(ResourceTypeDO.DeploymentManager, resourceName);
        resource.setDomain(DomainDO.fromFqdn(input.getDomain().getFqn()));
        resource.setEnvironmentClass(input.getEnvironmentClass().name());
        resource.setEnvironmentName(input.getEnvironmentName());
        resource.addProperty(new PropertyElement("hostname", vm.getHostName()));
        resource.addProperty(new PropertyElement("username", vm.getDeployUser()));
        resource.addProperty(new PropertyElement("password", vm.getDeployerPassword()));
        order.addStatuslogInfo("Updating Fasit with node " + vm.getHostName());
        try {
            fasitRestClient.registerResource(resource, "Bestilt i Basta av " + order.getCreatedBy());
        } catch (RuntimeException e) {
            logError(order, "Updating Fasit with deployment manager resource for host" + vm.getHostName() + " failed ", e);
        }
    }

    public void registerNode(NodeDO node, Order order) {
        fasitRestClient.setOnBehalfOf(User.getCurrentUser().getName());
        order.addStatuslogInfo("Updating Fasit with node " + node.getHostname());
        try {
            fasitRestClient.registerNode(node, "Bestilt i Basta av " + order.getCreatedBy());
        } catch (RuntimeException e) {
            logError(order, "Updating Fasit with node " + node.getHostname() + " failed ", e);
        }

    }

    public void removeFasitEntity(final Order order, String hostname) {
        try {
            fasitRestClient.setOnBehalfOf(User.getCurrentUser().getName());
            fasitRestClient.deleteNode(hostname, "Slettet " + hostname + " i Basta av " + order.getCreatedBy());
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
            NodeDO nodeDO = new NodeDO();
            nodeDO.setHostname(hostname);
            nodeDO.setStatus(LifeCycleStatusDO.STARTED);
            fasitRestClient.setOnBehalfOf(User.getCurrentUser().getName());
            fasitRestClient.updateNode(nodeDO, "Startet " + hostname + " i Basta av " + order.getCreatedBy());
            log.info("Started fasit entity for host " + hostname);
            order.addStatuslogInfo("Started Fasit entity for host " + hostname);
        } catch (Exception e) {
            log.error("Starting fasit entity for host " + hostname + " failed", e);
            order.addStatuslogWarning("Starting Fasit entity for host " + hostname + " failed");
        }
    }

    public void stopFasitEntity(Order order, String hostname) {
        try {
            NodeDO nodeDO = new NodeDO();
            nodeDO.setHostname(hostname);
            nodeDO.setStatus(LifeCycleStatusDO.STOPPED);
            fasitRestClient.setOnBehalfOf(User.getCurrentUser().getName());
            fasitRestClient.updateNode(nodeDO, "Stoppet " + hostname + " i Basta av " + order.getCreatedBy());
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
                orderId = fasitClient.createFasitResource(fasitResourcesUrl, toJson(resource), order.getCreatedBy(), comment);
            } else {
                log.debug("Updating resource in Fasit with id {}", id);
                final String updateUrl = fasitResourcesUrl + "/" + id;
                fasitClient.updateFasitResource(updateUrl, toJson(resource), order.getCreatedBy(), comment);
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
        log.info("Creating resource using v2 api {} ", resource.type, fasitResourcesUrl);
        try {
            final String comment = "Bestilt i Basta med jobb " + order.getId() + " av " + order.getCreatedBy();
            Optional<String> createdResourceId = fasitClient.createFasitResource(
                    fasitResourcesUrl,
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
        String url = fasitLifecycleApi + "/Resource/" + order.getId();
        String comment = resource.type + " is updated to " + state + " from Basta by order " + order.getId();
        fasitClient.updateFasitResource(url, toJson(new LifecyclePayload(state)), User.getCurrentUser().getName(), comment);

    }

    public boolean deleteResource(String id, String comment, Order order) {
        final String url = fasitResourcesUrl + "/" + id;
        Response fasitResponse = fasitClient.deleteFasitResource(url, User.getCurrentUser().getName(), comment);


        if (fasitResponse.getStatus() == NO_CONTENT.getStatusCode()) {
            order.addStatuslogSuccess("Successfully deleted resource with id " + id + " from fasit");
            return true;
        } else {
            final String message = "Unable to delete resource with id " + id + " from fasit. Got HTTPresponse " + fasitResponse.getStatus();
            log.error(message);
            order.addStatuslogWarning(message);
            return false;
        }
    }

    public static String toJson(Object payload) {
        try {
            return new Gson().toJson(payload);
        } catch (JsonIOException jioe) {
            throw new RuntimeException("Error serializing payload to JSON", jioe);
        }
    }
}