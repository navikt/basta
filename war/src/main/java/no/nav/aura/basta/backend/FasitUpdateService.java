package no.nav.aura.basta.backend;

import java.util.Optional;

import javax.inject.Inject;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.vm.Converters;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
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
        OrderStatusLog failure = new OrderStatusLog("Basta", message + " " + StatusLogHelper.abbreviateExceptionMessage(e), "registerinFasit", StatusLogLevel.warning);
        StatusLogHelper.addStatusLog(order, failure);
        log.error("Error updating Fasit with order " + order.getId(), e);
    }

    public void createWASDeploymentManagerResource(OrchestratorNodeDO vm, VMOrderInput input, String resourceName, Order order) {
        ResourceElement resource = new ResourceElement(ResourceTypeDO.DeploymentManager, resourceName);
        resource.setDomain(DomainDO.fromFqdn(input.getDomain().getFqn()));
        resource.setEnvironmentClass(input.getEnvironmentClass().name());
        resource.setEnvironmentName(input.getEnvironmentName());
        resource.addProperty(new PropertyElement("hostname", vm.getHostName()));
        resource.addProperty(new PropertyElement("username", vm.getDeployUser()));
        resource.addProperty(new PropertyElement("password", vm.getDeployerPassword()));
        StatusLogHelper.addStatusLog(order, new OrderStatusLog("Basta", "Updating Fasit with node " + vm.getHostName(), "fasit registration"));
        try {
            fasitRestClient.registerResource(resource, "Bestilt i Basta av " + order.getCreatedBy());
        } catch (RuntimeException e) {
            logError(order, "Updating Fasit with deployment manager resource for host" + vm.getHostName() + " failed ", e);
        }

    }

    public void registerNode(NodeDO node, Order order) {
        fasitRestClient.setOnBehalfOf(order.getCreatedBy());
        StatusLogHelper.addStatusLog(order, new OrderStatusLog("Basta", "Updating Fasit with node " + node.getHostname(), "fasit registration"));
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
        fasitNodeDO.setDataCenter(vm.getDatasenter());
        fasitNodeDO.setMemoryMb(vm.getMemoryMb());
        fasitNodeDO.setCpuCount(vm.getCpuCount());
        return fasitNodeDO;
    }

    public void removeFasitEntity(final Order order, String hostname) {
        try {
            fasitRestClient.setOnBehalfOf(order.getCreatedBy());
            fasitRestClient.deleteNode(hostname, "Slettet " + hostname + " i Basta av " + order.getCreatedBy());
            log.info("Delete fasit entity for host " + hostname);
            StatusLogHelper.addStatusLog(order, new OrderStatusLog("Basta", "Removed Fasit entity for host " + hostname, "removeFasitEntity"));
        } catch (Exception e) {
            log.error("Deleting fasit entity for host " + hostname + " failed", e);
            StatusLogHelper.addStatusLog(order, new OrderStatusLog("Basta", "Removing Fasit entity for host " + hostname + " failed", "removeFasitEntity", StatusLogLevel.warning));
        }

    }

    public void startFasitEntity(Order order, String hostname) {
        try {
            NodeDO nodeDO = new NodeDO();
            nodeDO.setHostname(hostname);
            nodeDO.setStatus(LifeCycleStatusDO.STARTED);
            fasitRestClient.setOnBehalfOf(order.getCreatedBy());
            fasitRestClient.updateNode(nodeDO, "Startet " + hostname + " i Basta av " + order.getCreatedBy());
            log.info("Started fasit entity for host " + hostname);
            StatusLogHelper.addStatusLog(order, new OrderStatusLog("Basta", "Started Fasit entity for host " + hostname, "startFasitEntity"));
        } catch (Exception e) {
            log.error("Starting fasit entity for host " + hostname + " failed", e);
            StatusLogHelper.addStatusLog(order, new OrderStatusLog("Basta", "Starting Fasit entity for host " + hostname + " failed", "startFasitEntity", StatusLogLevel.warning));
        }
    }

    public void stopFasitEntity(Order order, String hostname) {
        try {
            NodeDO nodeDO = new NodeDO();
            nodeDO.setHostname(hostname);
            nodeDO.setStatus(LifeCycleStatusDO.STOPPED);
            fasitRestClient.setOnBehalfOf(order.getCreatedBy());
            fasitRestClient.updateNode(nodeDO, "Stoppet " + hostname + " i Basta av " + order.getCreatedBy());
            log.info("Stopped fasit entity for host " + hostname);
            StatusLogHelper.addStatusLog(order, new OrderStatusLog("Basta", "Stopped Fasit entity for host " + hostname, "stopFasitEntity"));
        } catch (Exception e) {
            log.error("Stopping fasit entity for host " + hostname + " failed", e);
            StatusLogHelper.addStatusLog(order, new OrderStatusLog("Basta", "Stopping Fasit entity for host " + hostname + " failed", "stopFasitEntity", StatusLogLevel.warning));
        }
    }

    public Optional<ResourceElement> createResource(ResourceElement resource, Order order) {
        try {
            final ResourceElement createdResource = fasitRestClient.registerResource(resource, "Bestilt i Basta av " + order.getCreatedBy());
            final String message = "Successfully created Fasit resource " + resource.getAlias() + " (" + resource.getType().name() + ")";
            StatusLogHelper.addStatusLog(order, new OrderStatusLog("Basta", message, "registerInFasit", StatusLogLevel.success));
            log.info(message);
            return Optional.of(createdResource);
        } catch (RuntimeException e) {
            logError(order, "Creating Fasit resource failed", e);
            return Optional.empty();
        }
    }
}
