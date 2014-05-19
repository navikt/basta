package no.nav.aura.basta.backend;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import no.nav.aura.basta.Converters;
import no.nav.aura.basta.persistence.*;
import no.nav.aura.basta.rest.OrchestratorNodeDO;
import no.nav.aura.basta.util.SerializableFunction;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.NodeDO;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FasitUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(FasitUpdateService.class);

    private final FasitRestClient fasitRestClient;
    private final NodeRepository nodeRepository;
    private final SettingsRepository settingsRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final OrderRepository orderRepository;

    @Inject
    public FasitUpdateService(FasitRestClient fasitRestClient, NodeRepository nodeRepository, SettingsRepository settingsRepository, OrderStatusLogRepository orderStatusLogRepository, OrderRepository orderRepository) {
        this.fasitRestClient = fasitRestClient;
        this.nodeRepository = nodeRepository;
        this.settingsRepository = settingsRepository;
        this.orderStatusLogRepository = orderStatusLogRepository;
        this.orderRepository = orderRepository;
    }



    public void createFasitEntity(Order order, OrchestratorNodeDO vm, Node node) {
        try {
            Settings settings = settingsRepository.findByOrderId(order.getId());
            OrderStatusLog log = new OrderStatusLog(order, "Basta", "Updating Fasit whith node " + node.getHostname(), "basta:createFasistEntity", "success");
            switch (settings.getOrder().getNodeType()) {
            case APPLICATION_SERVER:
            case WAS_NODES:
                saveStatusLog(order,log);
                createNode(vm, node, settings);
                break;
            case WAS_DEPLOYMENT_MANAGER:
                saveStatusLog(order,log);
                createWASDeploymentManagerResource(vm, node, settings, "wasDmgr");
                break;
            case BPM_DEPLOYMENT_MANAGER:
                saveStatusLog(order,log);
                createWASDeploymentManagerResource(vm, node, settings, "bpmDmgr");
                break;
            case BPM_NODES:
                saveStatusLog(order,log);
                createNode(vm, node, settings);
                break;
            case PLAIN_LINUX:
                // Nothing to update
                break;
            default:
                throw new RuntimeException("Unable to update Fasit with node type " + settings.getOrder().getNodeType() + " for order " + order.getId());
            }
        } catch (RuntimeException e) {
            OrderStatusLog failure = new OrderStatusLog(order, "Basta", "Updating Fasit whith node " + node.getHostname() + "failed (Order id " + order.getId() + ")", "basta:createFasistEntity", "warning");
           saveStatusLog(order, failure);
            logger.error("Error updating Fasit with order " + order.getId(), e);
        }
    }

    private void saveStatusLog(Order order, OrderStatusLog log){
        orderStatusLogRepository.save(log);
        orderRepository.save(order);

    }

    private void createWASDeploymentManagerResource(OrchestratorNodeDO vm, Node node, Settings settings, String resourceName) {
        ResourceElement resource = new ResourceElement(ResourceTypeDO.DeploymentManager, resourceName);
        resource.setDomain(Converters.domainFrom(settings.getEnvironmentClass(), settings.getZone()));
        resource.setEnvironmentClass(settings.getEnvironmentClass().name());
        resource.setEnvironmentName(settings.getEnvironmentName());
        resource.addProperty(new PropertyElement("hostname", vm.getHostName()));
        resource.addProperty(new PropertyElement("username", vm.getDeployUser()));
        resource.addProperty(new PropertyElement("password", vm.getDeployerPassword()));
        resource = fasitRestClient.registerResource(resource, "Bestilt i Basta av " + settings.getCreatedBy());
        try {
            setUpdated(node, resource.getRef().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createNode(OrchestratorNodeDO vm, Node node, Settings settings) {
        NodeDO nodeDO = new NodeDO();
        nodeDO.setApplicationName(settings.getApplicationName());
        nodeDO.setDomain(Converters.domainFqdnFrom(settings.getEnvironmentClass(), settings.getZone()));
        nodeDO.setEnvironmentClass(Converters.fasitEnvironmentClassFromLocal(settings.getEnvironmentClass()).name());
        nodeDO.setEnvironmentName(settings.getEnvironmentName());
        nodeDO.setZone(settings.getZone().name());
        if (node.getAdminUrl() != null) {
            try {
                nodeDO.setAdminUrl(node.getAdminUrl().toURI());
            } catch (URISyntaxException e) {
                logger.warn("Unable to parse URI from URL " + node.getAdminUrl(), e);
            }
        }
        nodeDO.setHostname(node.getHostname());
        nodeDO.setUsername(vm.getDeployUser());
        nodeDO.setPassword(vm.getDeployerPassword());
        nodeDO.setPlatformType(Converters.platformTypeDOFrom(settings.getOrder().getNodeType(), node.getMiddleWareType()));
        nodeDO = fasitRestClient.registerNode(nodeDO, "Bestilt i Basta av " + settings.getCreatedBy());
        try {
            setUpdated(node, nodeDO.getRef().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setUpdated(Node node, URL fasitUrl) {
        node.setFasitUrl(fasitUrl);
        nodeRepository.save(node);
    }

    @SuppressWarnings("serial")
    public void removeFasitEntity(final Order order, String hosts) {
        FluentIterable<String> hostnames = DecommissionProperties.extractHostnames(hosts);
        for (String hostname : hostnames) {
            try {
                fasitRestClient.delete(hostname, "Slettet i Basta av " + order.getCreatedBy());
                logger.info("Delete fasit entity for host " + hostname);
                saveStatusLog(order, new OrderStatusLog(order, "Basta", "Removed Fasit entity for host " + hostname, "basta:decommission", ""));
            } catch (Exception e) {
                saveStatusLog(order, new OrderStatusLog(order, "Basta", "Removing Fasit entity for host " + hostname + "failed", "basta:decommission", "warning"));
                logger.error("Deleting fasit entity for host " + hostname + " failed", e);
            }
        }

        SerializableFunction<String, Iterable<Node>> retrieveNodes = new SerializableFunction<String, Iterable<Node>>() {
            public Iterable<Node> process(String hostname) {
                return nodeRepository.findByHostnameAndDecommissionOrderIdIsNull(hostname);
            }
        };
        ImmutableList<Node> nodes = hostnames.transformAndConcat(retrieveNodes).toList();
        for (Node node : nodes) {
            node.setDecommissionOrder(order);
            nodeRepository.save(node);
        }
    }
}
