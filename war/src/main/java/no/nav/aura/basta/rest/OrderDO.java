package no.nav.aura.basta.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.google.common.base.Joiner;
import no.nav.aura.basta.persistence.Node;
import no.nav.aura.basta.persistence.NodeType;
import no.nav.aura.basta.persistence.Order;
import no.nav.aura.basta.persistence.OrderType;

@XmlAccessorType(XmlAccessType.FIELD)
public class OrderDO extends ModelEntityDO {

    private String orchestratorOrderId;
    private URI uri;
    private String createdBy;
    private String createdByDisplayName;
    private OrderStatus status;
    private String errorMessage;
    private NodeType nodeType;
    private OrderType orderType;
    private List<NodeDO> nodes = new ArrayList<>();
    private String requestXml;
    private OrderDetailsDO settings;
    private Long nextOrderId;
    private Long previousOrderId;

    public OrderDO() {
        super();
    }



    public OrderDO(Order order, String requestXml, OrderDetailsDO settings, UriInfo uriInfo, Long previousOrderId, Long nextOrderId) {
        this(order, uriInfo);

        this.requestXml = requestXml;
        this.settings = settings;
        this.previousOrderId = previousOrderId;
        this.nextOrderId = nextOrderId;
    }

    public OrderDO(Order order, UriInfo uriInfo) {
        super(order);
        this.orderType = order.getOrderType();
        if (orderType.equals(OrderType.PROVISION)){
            this.nodeType = order.getNodeType();
        }else{
            this.nodeType = findNodeTypeOfProvisionedOrder(order);
        }
        this.status = order.getStatus();
        this.errorMessage = order.getErrorMessage();
        this.uri = UriFactory.createOrderUri(uriInfo, "getOrder", order.getId());
        this.orchestratorOrderId = order.getOrchestratorOrderId();
        this.createdBy = order.getCreatedBy();
        this.createdByDisplayName = order.getCreatedByDisplayName();
    }



    public void addAllNodesWithoutOrderReferences(Order order, UriInfo uriInfo){
        for (Node node : order.getNodes()) {
            this.nodes.add(new NodeDO(node, uriInfo,false));
        }
    }

    public void addAllNodesWithOrderReferences(Order order, UriInfo uriInfo){
        for (Node node : order.getNodes()) {
            this.nodes.add(new NodeDO(node, uriInfo,true));
        }
    }

    protected NodeType findNodeTypeOfProvisionedOrder(Order order) {
        NodeType candidate = null;
        for (Node node : order.getNodes()) {
            if (candidate != null && !node.getNodeType().equals(candidate)){
                candidate = NodeType.MULTIPLE;
            }else{
                candidate = NodeType.MULTIPLE.equals(candidate) ? NodeType.MULTIPLE : node.getNodeType();
            }
        }
        return candidate;
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

    public Long getNextOrderId() {
        return nextOrderId;
    }

    public Long getPreviousOrderId() {
        return previousOrderId;
    }

    public String getCreatedByDisplayName() {
        return createdByDisplayName;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public void setNextOrderId(Long nextOrderId) {
        this.nextOrderId = nextOrderId;
    }

    public void setPreviousOrderId(Long previousOrderId) {
        this.previousOrderId = previousOrderId;
    }
}
