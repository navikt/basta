package no.nav.aura.basta.backend;

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.vm.Converters;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.basta.security.User;
import no.nav.aura.basta.util.StatusLogHelper;
import no.nav.aura.envconfig.client.*;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FasitUpdateService {

    private static final Logger log = LoggerFactory.getLogger(FasitUpdateService.class);

    private final FasitRestClient fasitRestClient;

    @Inject
    public FasitUpdateService(FasitRestClient fasitRestClient) {
        this.fasitRestClient = fasitRestClient;
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

    public void removeFasitEntity(final Order order, String hostname) {
        try {
            fasitRestClient.setOnBehalfOf(User.getCurrentUser().getName());
            fasitRestClient.deleteNode(hostname, "Slettet " + hostname + " i Basta av " + order.getCreatedBy());
            log.info("Delete fasit entity for host " + hostname);
            order.addStatuslogInfo("Removed Fasit entity for host " + hostname);
        } catch (Exception e) {
            log.error("Deleting fasit entity for host " + hostname + " failed", e);
            order.addStatuslogWarning("Removing Fasit entity for host " + hostname + " failed");
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

    public Optional<ResourceElement> createResource(ResourceElement resource, Order order) {
        try {
            fasitRestClient.setOnBehalfOf(order.getCreatedBy());
            final ResourceElement createdResource = fasitRestClient.registerResource(resource, "Bestilt i Basta med jobb " + order.getId() + " av " + order.getCreatedBy());

            final String message = "Successfully created Fasit resource " + resource.getAlias() + " (" + resource.getType().name() + ")";
            order.addStatuslogSuccess(message);
            log.info(message);
            return Optional.of(createdResource);
        } catch (RuntimeException e) {
            logError(order, "Creating Fasit resource failed", e);
            return Optional.empty();
        }
    }

    public void updateResource(ResourceElement resource, LifeCycleStatusDO state, Order order) {
        order.getStatusLogs().add(new OrderStatusLog(resource.getType().name(), "Updating resource " + resource.getAlias() + "(" + resource.getId() + ") in fasit to " + state, "fasit"));
        ResourceElement updateObject = new ResourceElement(resource.getType(), resource.getAlias());
        updateObject.setLifeCycleStatus(state);
        fasitRestClient.setOnBehalfOf(User.getCurrentUser().getName());
        fasitRestClient.updateResource(resource.getId(), updateObject, resource.getType() + " is updated to " + state + " from Basta by order " + order.getId());
    }
    
    public boolean deleteResource(ResourceElement resource, Order order) {
        return deleteResource(resource.getId(),"Deleted by order " + order.getId() + " in Basta", order);
    }

    public boolean deleteResource(Long id, String comment, Order order) {
        fasitRestClient.setOnBehalfOf(order.getCreatedBy());
        final Response fasitResponse = fasitRestClient.deleteResource(id, comment);
        if (fasitResponse.getStatus() == 204) {
                order.addStatuslogSuccess("Successfully deleted resource with id " + id + " from Fasit");
            return true;
        } else {
            log.error("Unable to delete resource with id " + id + " from Fasit. Got response HTTP response " + fasitResponse.getStatus());
                order.addStatuslogWarning("Unable to delete resource with id " + id + " from Fasit. Got response HTTP response" + fasitResponse.getStatus());
           return false;
        }

    }
}
