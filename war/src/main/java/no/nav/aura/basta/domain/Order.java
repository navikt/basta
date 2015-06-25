package no.nav.aura.basta.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.ws.rs.core.UriInfo;

import no.nav.aura.basta.domain.input.Input;
import no.nav.aura.basta.domain.input.serviceuser.ServiceUserOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.domain.result.Result;
import no.nav.aura.basta.domain.result.serviceuser.ServiceUserResult;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;

import org.hibernate.annotations.BatchSize;

import com.google.common.collect.Maps;

@Entity
@Table(name = "ORDERTABLE")
@SequenceGenerator(name = "hibernate_sequence", sequenceName = "order_seq", allocationSize = 1)
public class Order extends ModelEntity {

    private String externalId;
    @Lob
    private String externalRequest;

    @Enumerated(EnumType.STRING)
    private OrderOperation orderOperation;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String errorMessage;

    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(name = "input_key")
    @Column(name = "input_value")
    @BatchSize(size = 500)
    @CollectionTable(name = "input_properties", joinColumns = @JoinColumn(name = "order_id"))
    private Map<String, String> inputs = Maps.newHashMap();

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "result_key")
    @Column(name = "result_value")
    @BatchSize(size = 500)
    @CollectionTable(name = "result_properties", joinColumns = @JoinColumn(name = "order_id"))
    private Map<String, String> results = new HashMap<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId")
    private Set<OrderStatusLog> statusLogs = new HashSet<>();

    public Order(OrderType orderType, OrderOperation orderOperation, Input input) {
        this.orderType = orderType;
        this.orderOperation = orderOperation;
        this.inputs = input.copy();
        this.status = OrderStatus.NEW;

    }

    @SuppressWarnings("unused")
    private Order() {
    }

    public OrderDO asOrderDO(final UriInfo uriInfo) {
        OrderDO orderDO = new OrderDO(this, uriInfo);
        orderDO.setInput(getInputAs(MapOperations.class).copy());

        return orderDO;

    }


    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getExternalRequest() {
        return externalRequest;
    }

    public void setExternalRequest(String externalRequest) {
        this.externalRequest = externalRequest;
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

    @Override
    public String toString() {
        return "Order{" +
                "externalId='" + externalId + '\'' +
                ", externalRequest='" + externalRequest + '\'' +
                ", status=" + status +
                ", errorMessage='" + errorMessage +
                '}';
    }

    public void setStatusIfMoreImportant(OrderStatus status) {
        if (status.isMoreImportantThan(this.getStatus())) {
            this.setStatus(status);
        }
    }

    public OrderStatusLog addStatusLog(OrderStatusLog log) {
        statusLogs.add(log);
        return log;
    }

    public Set<OrderStatusLog> getStatusLogs() {
        return statusLogs;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public OrderOperation getOrderOperation() {
        return orderOperation;
    }

    public void setOrderOperation(OrderOperation orderOperation) {
        this.orderOperation = orderOperation;
    }

    public <T extends MapOperations> T getInputAs(Class<T> inputClass) {
        return MapOperations.as(inputClass, inputs);
    }

    public <T extends MapOperations> T getResultAs(Class<T> resultClass) {
        return MapOperations.as(resultClass, results);
    }

    public Input getInput() {
        switch (orderType) {
        case VM:
            return getInputAs(VMOrderInput.class);
        case ServiceUser:
            return getInputAs(ServiceUserOrderInput.class);
        default:
            throw new IllegalArgumentException("Unknown ordertype " + orderType);

        }
    }

    public void setInput(MapOperations input) {
        this.inputs = input.copy();
    }


    public Result getResult() {
        switch (orderType) {
        case VM:
            return getResultAs(VMOrderResult.class);
        case ServiceUser:
            return getResultAs(ServiceUserResult.class);
        default:
            throw new IllegalArgumentException("Unknown ordertype " + orderType);

        }
    }
}
