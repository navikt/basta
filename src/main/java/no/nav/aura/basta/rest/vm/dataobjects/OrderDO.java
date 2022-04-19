package no.nav.aura.basta.rest.vm.dataobjects;

import no.nav.aura.basta.UriFactory;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.rest.dataobjects.ModelEntityDO;
import no.nav.aura.basta.rest.dataobjects.ResultDO;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
public class OrderDO extends ModelEntityDO {

    private OrderType orderType;
    private List<String> results;
    private Map<String, String> input;
    private String externalId;
    private URI uri;
    private String createdBy;
    private String createdByDisplayName;
    private OrderStatus status;
    private String errorMessage;
    private String orderDescription;
    private OrderOperation orderOperation;
    private List<ResultDO> resultDetails;

    public OrderDO() {
        super();
    }


    public OrderDO(Order order, UriInfo uriInfo) {
        super(order);
        this.orderOperation = order.getOrderOperation();
        this.orderType = order.getOrderType();
        this.status = order.getStatus();
        this.errorMessage = order.getErrorMessage();
        this.uri = UriFactory.createOrderUri(uriInfo, "getOrder", order.getId());
        this.externalId = order.getExternalId();
        this.createdBy = order.getCreatedBy();
        this.createdByDisplayName = order.getCreatedByDisplayName();
        this.results = order.getResult().keys();
        this.resultDetails = new ArrayList<>();
        String resultDescription = order.getResult().getDescription();
        this.orderDescription = resultDescription != null ? resultDescription : order.getInput().getOrderDescription();
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

    public String getCreatedByDisplayName() {
        return createdByDisplayName;
    }

    public OrderOperation getOrderOperation() {
        return orderOperation;
    }

    public void setOrderOperation(OrderOperation orderOperation) {
        this.orderOperation = orderOperation;
    }

    public Map<String, String> getInput() {
        return input;
    }

    public void setInput(Map<String, String> input) {
        this.input = input;
    }

    public List<String> getResults() {
        return results;
    }

    public void setResults(List<String> results) {
        this.results = results;
    }

    public void addResultHistory(ResultDO result) {
        resultDetails.add(result);
    }

    public  List<ResultDO> getResultDetails(){
        return resultDetails;
    }

    public void setResultDetails(List<ResultDO> resultDetails){
        this.resultDetails = resultDetails;

    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }
}
