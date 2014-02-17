package no.nav.aura.basta.backend;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;

import no.nav.aura.basta.Converters;
import no.nav.aura.basta.persistence.DecommissionProperties;
import no.nav.aura.basta.persistence.Node;
import no.nav.aura.basta.persistence.NodeRepository;
import no.nav.aura.basta.persistence.Order;
import no.nav.aura.basta.persistence.Settings;
import no.nav.aura.basta.persistence.SettingsRepository;
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

    @Inject
    public FasitUpdateService(FasitRestClient fasitRestClient, NodeRepository nodeRepository, SettingsRepository settingsRepository) {
        this.fasitRestClient = fasitRestClient;
        this.nodeRepository = nodeRepository;
        this.settingsRepository = settingsRepository;
    }

    public void createFasitEntity(Long orderId, OrchestratorNodeDO vm, Node node) {
        try {
            Settings settings = settingsRepository.findByOrderId(orderId);
            switch (settings.getOrder().getNodeType()) {
            case APPLICATION_SERVER:
                createNode(vm, node, settings);
                break;
            case WAS_DEPLOYMENT_MANAGER:
                createWASDeploymentManagerResource(vm, node, settings, "wasDmgr");
                break;
            case BPM_DEPLOYMENT_MANAGER:
                createWASDeploymentManagerResource(vm, node, settings, "bpmDmgr");
                break;
            case BPM_NODES:
                createNode(vm, node, settings);
                break;
            case PLAIN_LINUX:
                // Nothing to update
                break;
            default:
                throw new RuntimeException("Unable to update Fasit with node type " + settings.getOrder().getNodeType() + " for order " + orderId);
            }
        } catch (RuntimeException e) {
            logger.error("Error updating Fasit with order " + orderId, e);
        }
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
        SerializableFunction<String, Iterable<Node>> retrieveNodes = new SerializableFunction<String, Iterable<Node>>() {
            public Iterable<Node> process(String hostname) {
                return nodeRepository.findByHostname(hostname);
            }
        };
        for (Node node : DecommissionProperties.extractHostnames(hosts).transformAndConcat(retrieveNodes)) {
            // TODO: Is this the right place for setting the decommission order on the nodes?
            node.setDecommissionOrder(order);
            nodeRepository.save(node);
        }
        for (Node node : DecommissionProperties.extractHostnames(hosts).transformAndConcat(retrieveNodes)) {
            if (node.getFasitUrl() != null) {
                try {
                    fasitRestClient.delete(node.getFasitUrl().toURI(), "Slettet i Basta av " + order.getCreatedBy());
                    logger.info("Delete fasit entity for host " + node.getHostname());
                } catch (Exception e) {
                    logger.info("Deleting fasit entity for host " + node.getHostname() + " failed", e);
                }
            }
        }
    }
}
