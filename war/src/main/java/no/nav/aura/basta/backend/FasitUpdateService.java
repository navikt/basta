package no.nav.aura.basta.backend;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.vm.Converters;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.rest.vm.dataobjects.OrchestratorNodeDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.LifeCycleStatusDO;
import no.nav.aura.envconfig.client.NodeDO;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FasitUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(FasitUpdateService.class);

    private final FasitRestClient fasitRestClient;
    private final OrderRepository orderRepository;

    @Inject
    public FasitUpdateService(FasitRestClient fasitRestClient, OrderRepository orderRepository) {
        this.fasitRestClient = fasitRestClient;
        this.orderRepository = orderRepository;
    }

    public void createFasitEntity(Order order, OrchestratorNodeDO vm) {
        try {
            URL fasitURL = null;
            VMOrderInput input = order.getInputAs(VMOrderInput.class);
            fasitRestClient.setOnBehalfOf(order.getCreatedBy());
            OrderStatusLog log = new OrderStatusLog("Basta", "Updating Fasit with node " + vm.getHostName(), "createFasitEntity");
            NodeType nodeType = order.getInputAs(VMOrderInput.class).getNodeType();

            switch (nodeType) {
            case JBOSS:
            case WAS_NODES:
            case BPM_NODES:
                fasitURL = registerNodeDOInFasit(vm, input, input.getNodeType(), order.getCreatedBy());
                break;
            case WAS_DEPLOYMENT_MANAGER:
                fasitURL = createWASDeploymentManagerResource(vm, input, "wasDmgr", order.getCreatedBy());
                break;
            case BPM_DEPLOYMENT_MANAGER:
                fasitURL = createWASDeploymentManagerResource(vm, input, "bpmDmgr", order.getCreatedBy());
                break;
            case PLAIN_LINUX:
                // Nothing to update
                break;
            default:
                throw new RuntimeException("Unable to update Fasit with node type " + nodeType + " for order " + order.getId());
            }
            if (fasitURL != null) {
                // node.setResultUrl(fasitURL); TODO remove this?
                addStatus(order, log);
            }
        } catch (RuntimeException e) {
            OrderStatusLog failure = new OrderStatusLog("Basta", "Updating Fasit with node " + vm.getHostName() + " failed " + abbreviateExceptionMessage(e), "createFasitEntity", StatusLogLevel.warning);
            addStatus(order, failure);
            logger.error("Error updating Fasit with order " + order.getId(), e);
        }
    }

    String abbreviateExceptionMessage(RuntimeException e) {
        if (e.getMessage() != null && e.getMessage().length() > 3) {
            return ": " + StringUtils.abbreviate(e.getMessage(), 158);
        }
        return e.getMessage();
    }

    void addStatus(Order order, OrderStatusLog log) {
        order.addStatusLog(log);
        order.setStatusIfMoreImportant(OrderStatus.fromString(log.getStatusOption()));
    }

    private URL createWASDeploymentManagerResource(OrchestratorNodeDO vm, VMOrderInput input, String resourceName, String createdBy) {
        ResourceElement resource = new ResourceElement(ResourceTypeDO.DeploymentManager, resourceName);
        resource.setDomain(Converters.domainFrom(input.getEnvironmentClass(), input.getZone()));
        resource.setEnvironmentClass(input.getEnvironmentClass().name());
        resource.setEnvironmentName(input.getEnvironmentName());
        resource.addProperty(new PropertyElement("hostname", vm.getHostName()));
        resource.addProperty(new PropertyElement("username", vm.getDeployUser()));
        resource.addProperty(new PropertyElement("password", vm.getDeployerPassword()));
        resource = fasitRestClient.registerResource(resource, "Bestilt i Basta av " + createdBy);
        try {
            return resource.getRef().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private URL registerNodeDOInFasit(OrchestratorNodeDO vm, VMOrderInput settings, NodeType nodeType, String createdBy) {
        NodeDO fasitNodeDO = new NodeDO();
        fasitNodeDO.setDomain(Converters.domainFqdnFrom(settings.getEnvironmentClass(), settings.getZone()));
        fasitNodeDO.setEnvironmentClass(Converters.fasitEnvironmentClassFromLocal(settings.getEnvironmentClass()).name());
        fasitNodeDO.setEnvironmentName(settings.getEnvironmentName());
        fasitNodeDO.setApplicationMappingName(settings.getApplicationMappingName());
        fasitNodeDO.setZone(settings.getZone().name());
        if (vm.getAdminUrl() != null) {
            try {
                fasitNodeDO.setAdminUrl(vm.getAdminUrl().toURI());
            } catch (URISyntaxException e) {
                logger.warn("Unable to parse URI from URL " + vm.getAdminUrl(), e);
            }
        }
        fasitNodeDO.setHostname(vm.getHostName());
        fasitNodeDO.setUsername(vm.getDeployUser());
        fasitNodeDO.setPassword(vm.getDeployerPassword());
        fasitNodeDO.setPlatformType(Converters.platformTypeDOFrom(nodeType, vm.getMiddlewareType()));
        fasitNodeDO.setDataCenter(vm.getDatasenter());
        fasitNodeDO.setMemoryMb(vm.getMemoryMb());
        fasitNodeDO.setCpuCount(vm.getCpuCount());
        fasitNodeDO = fasitRestClient.registerNode(fasitNodeDO, "Bestilt i Basta av " + createdBy);
        try {
            return fasitNodeDO.getRef().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("serial")
    public void removeFasitEntity(final Order order, String hostname) {
        try {
            fasitRestClient.setOnBehalfOf(order.getCreatedBy());
            fasitRestClient.deleteNode(hostname, "Slettet " + hostname + " i Basta av " + order.getCreatedBy());
            logger.info("Delete fasit entity for host " + hostname);
            addStatus(order, new OrderStatusLog("Basta", "Removed Fasit entity for host " + hostname, "removeFasitEntity"));
        } catch (Exception e) {
            logger.error("Deleting fasit entity for host " + hostname + " failed", e);
            addStatus(order, new OrderStatusLog("Basta", "Removing Fasit entity for host " + hostname + " failed", "removeFasitEntity", StatusLogLevel.warning));
        }

    }

    public void startFasitEntity(Order order, String hostname) {
        try {
            NodeDO nodeDO = new NodeDO();
            nodeDO.setHostname(hostname);
            nodeDO.setStatus(LifeCycleStatusDO.STARTED);
            fasitRestClient.setOnBehalfOf(order.getCreatedBy());
            fasitRestClient.updateNode(nodeDO, "Startet " + hostname + " i Basta av " + order.getCreatedBy());
            logger.info("Started fasit entity for host " + hostname);
            addStatus(order, new OrderStatusLog("Basta", "Started Fasit entity for host " + hostname, "startFasitEntity"));
        } catch (Exception e) {
            logger.error("Starting fasit entity for host " + hostname + " failed", e);
            addStatus(order, new OrderStatusLog("Basta", "Starting Fasit entity for host " + hostname + " failed", "startFasitEntity", StatusLogLevel.warning));
        }
    }

    public void stopFasitEntity(Order order, String hostname) {
        try {
            NodeDO nodeDO = new NodeDO();
            nodeDO.setHostname(hostname);
            nodeDO.setStatus(LifeCycleStatusDO.STOPPED);
            fasitRestClient.setOnBehalfOf(order.getCreatedBy());
            fasitRestClient.updateNode(nodeDO, "Stoppet " + hostname + " i Basta av " + order.getCreatedBy());
            logger.info("Stopped fasit entity for host " + hostname);
            addStatus(order, new OrderStatusLog("Basta", "Stopped Fasit entity for host " + hostname, "stopFasitEntity"));
        } catch (Exception e) {
            logger.error("Stopping fasit entity for host " + hostname + " failed", e);
            addStatus(order, new OrderStatusLog("Basta", "Stopping Fasit entity for host " + hostname + " failed", "stopFasitEntity", StatusLogLevel.warning));
        }
    }
}
