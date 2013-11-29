package no.nav.aura.basta.rest;

import java.net.URI;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.basta.persistence.Order;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderDO {

    private Long id;
    private String orchestratorOrderId;
    private URI uri;
    private String createdBy;
    private URI requestXmlUri;
    private URI settingsUri;
    private URI nodesUri;

    public OrderDO() {
    }

    public OrderDO(Order order, UriInfo uriInfo) {
        this.uri = UriFactory.createOrderUri(uriInfo, "getOrder", order.getId());
        this.nodesUri = UriFactory.createOrderUri(uriInfo, "getNodes", order.getId());
        this.requestXmlUri = UriFactory.createOrderUri(uriInfo, "getRequestXml", order.getId());
        this.id = order.getId();
        this.orchestratorOrderId = order.getOrchestratorOrderId();
        this.settingsUri = UriFactory.createOrderUri(uriInfo, "getSettings", order.getId());
        this.createdBy = order.getCreatedBy();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
