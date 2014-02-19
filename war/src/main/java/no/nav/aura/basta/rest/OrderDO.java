package no.nav.aura.basta.rest;

import java.net.URI;

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
    private URI requestXmlUri;
    private URI settingsUri;
    private URI nodesUri;
    private String errorMessage;
    private NodeType nodeType;

    public OrderDO() {
        super();
    }

    public OrderDO(Order order, UriInfo uriInfo) {
        super(order);
        this.nodeType = order.getNodeType();
        this.status = order.getStatus();
        this.errorMessage = order.getErrorMessage();
        this.uri = UriFactory.createOrderUri(uriInfo, "getOrder", order.getId());
        this.nodesUri = UriFactory.createOrderUri(uriInfo, "getNodes", order.getId());
        this.requestXmlUri = UriFactory.createOrderUri(uriInfo, "getRequestXml", order.getId());
        this.orchestratorOrderId = order.getOrchestratorOrderId();
        this.settingsUri = UriFactory.createOrderUri(uriInfo, "getSettings", order.getId());
        this.createdBy = order.getCreatedBy();
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

    public URI getRequestXmlUri() {
        return requestXmlUri;
    }

    public void setRequestXmlUri(URI requestXmlUri) {
        this.requestXmlUri = requestXmlUri;
    }

    public URI getSettingsUri() {
        return settingsUri;
    }

    public void setSettingsUri(URI settingsUri) {
        this.settingsUri = settingsUri;
    }

    public URI getNodesUri() {
        return nodesUri;
    }

    public void setNodesUri(URI nodesUri) {
        this.nodesUri = nodesUri;
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

}
