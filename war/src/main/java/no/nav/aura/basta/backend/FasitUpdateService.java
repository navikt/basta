package no.nav.aura.basta.backend;

import no.nav.aura.basta.domain.input.vm.Converters;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.persistence.*;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.OrchestratorNodeDO;
import no.nav.aura.basta.rest.OrderStatus;
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

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Component
public class FasitUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(FasitUpdateService.class);

    private final FasitRestClient fasitRestClient;
    private final NodeRepository nodeRepository;
    private final OrderRepository orderRepository;

    @Inject
    public FasitUpdateService(FasitRestClient fasitRestClient, NodeRepository nodeRepository,OrderRepository orderRepository) {
        this.fasitRestClient = fasitRestClient;
        this.nodeRepository = nodeRepository;
        this.orderRepository = orderRepository;
    }



    public void createFasitEntity(Order order, OrchestratorNodeDO vm, Node node) {
        try {
            URL fasitURL = null;
            VMOrderInput input = order.getInputAs(VMOrderInput.class);
            OrderStatusLog log = new OrderStatusLog("Basta", "Updating Fasit with node " + node.getHostname(), "createFasitEntity", "");
            switch (order.getNodeType()) {
                case APPLICATION_SERVER:
                case WAS_NODES:
                case BPM_NODES:
                    fasitURL = registerNodeDOInFasit(vm, node, input, order.getNodeType(), order.getCreatedBy());
                    break;
                case WAS_DEPLOYMENT_MANAGER:
                    fasitURL  = createWASDeploymentManagerResource(vm, node, input, "wasDmgr", order.getCreatedBy());
                    break;
                case BPM_DEPLOYMENT_MANAGER:
                    fasitURL = createWASDeploymentManagerResource(vm, node, input, "bpmDmgr", order.getCreatedBy());
                    break;
                case PLAIN_LINUX:
                    // Nothing to update
                    break;
            default:
                throw new RuntimeException("Unable to update Fasit with node type " + order.getNodeType() + " for order " + order.getId());
            }
            if(fasitURL != null){
                node.setFasitUrl(fasitURL);
                addStatus(order, log);
            }
        } catch (RuntimeException e) {
            OrderStatusLog failure = new OrderStatusLog("Basta", "Updating Fasit with node " + node.getHostname() + " failed " + abbreviateExceptionMessage(e) , "createFasitEntity", "warning");
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

    void addStatus(Order order, OrderStatusLog log){
        order.addStatusLog(log);
        order.setStatusIfMoreImportant(OrderStatus.fromString(log.getStatusOption()));
    }

    private URL createWASDeploymentManagerResource(OrchestratorNodeDO vm, Node node, VMOrderInput inputResolver, String resourceName, String createdBy) {
        ResourceElement resource = new ResourceElement(ResourceTypeDO.DeploymentManager, resourceName);
        resource.setDomain(Converters.domainFrom(inputResolver.getEnvironmentClass(), inputResolver.getZone()));
        resource.setEnvironmentClass(inputResolver.getEnvironmentClass().name());
        resource.setEnvironmentName(inputResolver.getEnvironmentName());
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

    private URL registerNodeDOInFasit(OrchestratorNodeDO vm, Node node, VMOrderInput settings, NodeType nodeType, String createdBy) {
        NodeDO fasitNodeDO = new NodeDO();
        fasitNodeDO.setDomain(Converters.domainFqdnFrom(settings.getEnvironmentClass(), settings.getZone()));
        fasitNodeDO.setEnvironmentClass(Converters.fasitEnvironmentClassFromLocal(settings.getEnvironmentClass()).name());
        fasitNodeDO.setEnvironmentName(settings.getEnvironmentName());
        fasitNodeDO.setApplicationMappingName(settings.getApplicationMappingName());
        fasitNodeDO.setZone(settings.getZone().name());
        if (node.getAdminUrl() != null) {
            try {
                fasitNodeDO.setAdminUrl(node.getAdminUrl().toURI());
            } catch (URISyntaxException e) {
                logger.warn("Unable to parse URI from URL " + node.getAdminUrl(), e);
            }
        }
        fasitNodeDO.setHostname(node.getHostname());
        fasitNodeDO.setUsername(vm.getDeployUser());
        fasitNodeDO.setPassword(vm.getDeployerPassword());
        fasitNodeDO.setPlatformType(Converters.platformTypeDOFrom(nodeType, node.getMiddleWareType()));
        fasitNodeDO.setDataCenter(node.getDatasenter());
        fasitNodeDO.setMemoryMb(node.getMemoryMb());
        fasitNodeDO.setCpuCount(node.getCpuCount());
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
                fasitRestClient.deleteNode(hostname, "Slettet i Basta av " + order.getCreatedBy());
                logger.info("Delete fasit entity for host " + hostname);
                addStatus(order, new OrderStatusLog("Basta", "Removed Fasit entity for host " + hostname, "removeFasitEntity", ""));
            } catch (Exception e) {
                logger.error("Deleting fasit entity for host " + hostname + " failed", e);
                addStatus(order, new OrderStatusLog("Basta", "Removing Fasit entity for host " + hostname + " failed", "removeFasitEntity", "warning"));
            }




    }

    public void startFasitEntity(Order order, String hostname) {
        try{
            NodeDO nodeDO = new NodeDO();
            nodeDO.setHostname(hostname);
            nodeDO.setStatus(LifeCycleStatusDO.STARTED);
            fasitRestClient.updateNode(nodeDO, "Startet i Basta av " + order.getCreatedBy());
            logger.info("Started fasit entity for host " + hostname);
            addStatus(order, new OrderStatusLog("Basta", "Started Fasit entity for host " + hostname, "startFasitEntity", ""));
        } catch (Exception e) {
            logger.error("Starting fasit entity for host " + hostname + " failed", e);
            addStatus(order, new OrderStatusLog("Basta", "Starting Fasit entity for host " + hostname + " failed", "startFasitEntity", "warning"));
        }
    }


    public void stopFasitEntity(Order order, String hostname) {
        try{
            NodeDO nodeDO = new NodeDO();
            nodeDO.setHostname(hostname);
            nodeDO.setStatus(LifeCycleStatusDO.STOPPED);
            fasitRestClient.updateNode(nodeDO, "Stoppet i Basta av " + order.getCreatedBy());
            logger.info("Stopped fasit entity for host " + hostname);
            addStatus(order, new OrderStatusLog("Basta", "Stopped Fasit entity for host " + hostname, "stopFasitEntity", ""));
        } catch (Exception e) {
            logger.error("Stopping fasit entity for host " + hostname + " failed", e);
            addStatus(order, new OrderStatusLog("Basta", "Stopping Fasit entity for host " + hostname + " failed", "stopFasitEntity", "warning"));
        }
    }
}
