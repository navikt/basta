package no.nav.aura.basta.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.result.vm.VMNode;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;

@XmlAccessorType(XmlAccessType.FIELD)
public class OrderDO extends ModelEntityDO {

    private String externalId;
    private URI uri;
    private String createdBy;
    private String createdByDisplayName;
    private OrderStatus status;
    private String errorMessage;
    private NodeType nodeType;
    private OrderType orderType;
    private List<NodeDO> nodes = new ArrayList<>();
    private String externalRequest;
    private OrderDetailsDO settings;
    private Long nextOrderId;
    private Long previousOrderId;

    public OrderDO() {
        super();
    }


    public OrderDO(Order order, UriInfo uriInfo) {
        super(order);
        this.orderType = order.getOrderType();
        if (orderType.equals(OrderType.PROVISION)) {
            this.nodeType = order.getNodeType();
        } else {
            //this.nodeType = findNodeTypeOfProvisionedOrder(order);
        }
        this.status = order.getStatus();
        this.errorMessage = order.getErrorMessage();
        this.uri = UriFactory.createOrderUri(uriInfo, "getOrder", order.getId());
        this.externalId = order.getExternalId();
        this.createdBy = order.getCreatedBy();
        this.createdByDisplayName = order.getCreatedByDisplayName();
        addAllNodesWithoutOrderReferences(order, uriInfo);

    }

    public void addAllNodesWithoutOrderReferences(Order order, UriInfo uriInfo) {
        for (VMNode vmNode: order.getResultAs(VMOrderResult.class).asNodes()) {
            this.nodes.add(new NodeDO(vmNode, uriInfo));
        }
    }



    /*
    public void addAllNodesWithOrderReferences(Order order, UriInfo uriInfo) {
        for (Node node : order.getNodes()) {
            this.nodes.add(new NodeDO(node, uriInfo, true));
        }
    }

    protected NodeType findNodeTypeOfProvisionedOrder(Order order) {
        NodeType candidate = null;
        for (Node node : order.getNodes()) {
            if (candidate != null && !node.getNodeType().equals(candidate)) {
                candidate = NodeType.MULTIPLE;
            } else {
                candidate = NodeType.MULTIPLE.equals(candidate) ? NodeType.MULTIPLE : node.getNodeType();
            }
        }
        return candidate;
    }*/

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
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

    public String getExternalRequest() {
        return externalRequest;
    }

    public void setExternalRequest(String requestXml) {
        this.externalRequest = requestXml;
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
