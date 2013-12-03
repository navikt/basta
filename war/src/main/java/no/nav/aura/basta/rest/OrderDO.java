package no.nav.aura.basta.rest;

import java.net.URI;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.basta.persistence.Order;

@XmlAccessorType(XmlAccessType.FIELD)
public class OrderDO extends ModelEntityDO {

    private String orchestratorOrderId;
    private URI uri;
    private String createdBy;
    private URI requestXmlUri;
    private URI settingsUri;
    private URI nodesUri;

    public OrderDO() {
        super();
    }

    public OrderDO(Order order, UriInfo uriInfo) {
        super(order);
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

}
