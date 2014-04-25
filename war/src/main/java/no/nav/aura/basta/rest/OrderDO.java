package no.nav.aura.basta.rest;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.basta.persistence.NodeType;
import no.nav.aura.basta.persistence.Order;

@XmlAccessorType(XmlAccessType.FIELD)
public class OrderDO extends ModelEntityDO {

    private String orchestratorOrderId;
    private URI uri;
    private String createdBy;
    private OrderStatus status;
    private String errorMessage;
    private NodeType nodeType;
    private List<NodeDO> nodes;
    private String requestXml;
    private OrderDetailsDO settings;
    private String bootstrapClass;
    private Long nextOrderId;
    private Long previousOrderId;

    public OrderDO() {
        super();
    }

    public OrderDO(Order order, List<NodeDO> nodes, String requestXml, OrderDetailsDO settings, UriInfo uriInfo, Long previousOrderId, Long nextOrderId) {
        this(order, uriInfo);
        this.nodes = nodes;
        this.requestXml = requestXml;
        this.settings = settings;
        this.previousOrderId = previousOrderId;
        this.nextOrderId = nextOrderId;
    }

    public OrderDO(Order order, UriInfo uriInfo) {
        super(order);
        this.nodeType = order.getNodeType();
        this.status = order.getStatus();
        this.errorMessage = order.getErrorMessage();
        this.uri = UriFactory.createOrderUri(uriInfo, "getOrder", order.getId());
        this.orchestratorOrderId = order.getOrchestratorOrderId();
        this.createdBy = order.getCreatedBy();
        this.bootstrapClass=order.getStatus().getBootstrapClass();
    }

    public String getOrchestratorOrderId() {
        return orchestratorOrderId;
    }

    public void setOrchestratorOrderId(String orchestratorOrderId) {
        this.orchestratorOrderId = orchestratorOrderId;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public List<NodeDO> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeDO> nodes) {
        this.nodes = nodes;
    }

    public String getRequestXml() {
        return requestXml;
    }

    public void setRequestXml(String requestXml) {
        this.requestXml = requestXml;
    }

    public OrderDetailsDO getSettings() {
        return settings;
    }

    public void setSettings(OrderDetailsDO settings) {
        this.settings = settings;
    }

    public String getBootstrapClass() {
        return bootstrapClass;
    }

    public Long getNextOrderId() {
        return nextOrderId;
    }

    public Long getPreviousOrderId() {
        return previousOrderId;
    }
}
