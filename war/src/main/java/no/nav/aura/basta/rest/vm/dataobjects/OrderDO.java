package no.nav.aura.basta.rest.vm.dataobjects;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.result.vm.VMNode;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.rest.dataobjects.ModelEntityDO;
import no.nav.aura.basta.UriFactory;

@XmlAccessorType(XmlAccessType.FIELD)
public class OrderDO extends ModelEntityDO {

    private Map<String, String> input;
    private String externalId;
    private URI uri;
    private String createdBy;
    private String createdByDisplayName;
    private OrderStatus status;
    private String errorMessage;
    private String orderDescription;
    private OrderOperation orderOperation;
    private List<NodeDO> nodes = new ArrayList<>();
    private String externalRequest;
    private Long nextOrderId;
    private Long previousOrderId;

    public OrderDO() {
        super();
    }


    public OrderDO(Order order, UriInfo uriInfo) {
        super(order);
        this.orderOperation = order.getOrderOperation();
        this.orderDescription = order.getInput().getOrderDescription();
        this.status = order.getStatus();
        this.errorMessage = order.getErrorMessage();
        this.uri = UriFactory.createOrderUri(uriInfo, "getOrder", order.getId());
        this.externalId = order.getExternalId();
        this.createdBy = order.getCreatedBy();
        this.createdByDisplayName = order.getCreatedByDisplayName();
        this.input = order.getInputAs(MapOperations.class).copy();


        addAllNodesWithoutOrderReferences(order, uriInfo);
    }

    public void addAllNodesWithoutOrderReferences(Order order, UriInfo uriInfo) {
        for (VMNode vmNode: order.getResultAs(VMOrderResult.class).asNodes()) {
            this.nodes.add(new NodeDO(vmNode, uriInfo));
        }
    }


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

    public String getOrderDescription() {
        return orderDescription;
    }

    public void setOrderDescription(String orderDescription) {
        this.orderDescription = orderDescription;
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

    public Long getNextOrderId() {
        return nextOrderId;
    }

    public Long getPreviousOrderId() {
        return previousOrderId;
    }

    public String getCreatedByDisplayName() {
        return createdByDisplayName;
    }

    public OrderOperation getOrderOperation() {
        return orderOperation;
    }

    public void setOrderOperation(OrderOperation orderOperation) {
        this.orderOperation = orderOperation;
    }

    public void setNextOrderId(Long nextOrderId) {
        this.nextOrderId = nextOrderId;
    }

    public void setPreviousOrderId(Long previousOrderId) {
        this.previousOrderId = previousOrderId;
    }

    public Map<String, String> getInput() {
        return input;
    }

    public void setInput(Map<String, String> input) {
        this.input = input;
    }
}
