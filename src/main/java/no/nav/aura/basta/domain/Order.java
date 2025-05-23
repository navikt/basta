package no.nav.aura.basta.domain;

import java.util.*;

import jakarta.persistence.*;
import jakarta.ws.rs.core.UriInfo;

import no.nav.aura.basta.domain.input.Input;
import no.nav.aura.basta.domain.input.bigip.BigIPOrderInput;
import no.nav.aura.basta.domain.input.database.DBOrderInput;
import no.nav.aura.basta.domain.input.mq.MqOrderInput;
import no.nav.aura.basta.domain.input.serviceuser.GroupOrderInput;
import no.nav.aura.basta.domain.input.serviceuser.ServiceUserOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.domain.result.Result;
import no.nav.aura.basta.domain.result.bigip.BigIPOrderResult;
import no.nav.aura.basta.domain.result.database.DBOrderResult;
import no.nav.aura.basta.domain.result.mq.MqOrderResult;
import no.nav.aura.basta.domain.result.serviceuser.GroupResult;
import no.nav.aura.basta.domain.result.serviceuser.ServiceUserResult;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;

import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "ORDERTABLE")
@SequenceGenerator(name = "hibernate_sequence", sequenceName = "order_seq", allocationSize = 1)
public class Order extends ModelEntity {

    private String externalId = "N/A";
    @Lob
    private String externalRequest;

    @Enumerated(EnumType.STRING)
    private OrderOperation orderOperation;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String errorMessage;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "input_key")
    @Column(name = "input_value")
    @BatchSize(size = 1000)
    @CollectionTable(name = "input_properties", joinColumns = @JoinColumn(name = "order_id") )
    private Map<String, String> inputs = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "result_key")
    @Column(name = "result_value")
    @BatchSize(size = 1000)
    @CollectionTable(name = "result_properties", joinColumns = @JoinColumn(name = "order_id") )
    private Map<String, String> results = new HashMap<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId")
    private List<OrderStatusLog> statusLogs = new ArrayList<>();

    public Order(OrderType orderType, OrderOperation orderOperation, Input input) {
        this(orderType, orderOperation, input.copy());
    }

    public Order(OrderType orderType, OrderOperation orderOperation, Map<String, String> input) {
        this.orderType = orderType;
        this.orderOperation = orderOperation;
        this.inputs = input;
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

    public Order addStatuslog(String message, StatusLogLevel statusLogLevel) {
        return addLog(new OrderStatusLog(message, statusLogLevel));
    }

    public Order addStatuslogInfo(String message) {
        return addLog(new OrderStatusLog(message, StatusLogLevel.info));
    }

    public Order addStatuslogSuccess(String message) {
        return addLog(new OrderStatusLog(message, StatusLogLevel.success));
    }

    public Order addStatuslogError(String message) {
        return addLog(new OrderStatusLog(message, StatusLogLevel.error));
    }

    public Order addStatuslogWarning(String message) {
        return addLog(new OrderStatusLog(message, StatusLogLevel.warning));
    }

    private Order addLog(OrderStatusLog log){
        statusLogs.add(log);
        setStatusIfMoreImportant(OrderStatus.fromStatusLogLevel(log.getStatusOption()));
        return this;
    }

    public List<OrderStatusLog> getStatusLogs() {
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

    public Map<String, String> getResults() {
        return results;
    }

    public void setResults(Map<String, String> results) {
        this.results = results;
    }

    public Map<String, String> getInputs() {
        return inputs;
    }

    public Input getInput() {
        switch (orderType) {
        case VM:
            return getInputAs(VMOrderInput.class);
        case ServiceUser:
            return getInputAs(ServiceUserOrderInput.class);
            case OracleDB:
            return getInputAs(DBOrderInput.class);
        case MQ:
            return getInputAs(MqOrderInput.class);
        case BIGIP:
            return getInputAs(BigIPOrderInput.class);
        case AdGroup:
            return getInputAs(GroupOrderInput.class);
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
            case OracleDB:
            return getResultAs(DBOrderResult.class);
        case MQ:
            return getResultAs(MqOrderResult.class);
        case BIGIP:
            return getResultAs(BigIPOrderResult.class);
        case AdGroup:
            return getResultAs(GroupResult.class);
        default:
            throw new IllegalArgumentException("Unknown ordertype " + orderType);
        }
    }

    public void log(String message, StatusLogLevel level) {
        statusLogs.add(new OrderStatusLog(message, level));

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(externalId, order.externalId) &&
                Objects.equals(externalRequest, order.externalRequest) &&
                orderOperation == order.orderOperation &&
                orderType == order.orderType &&
                status == order.status &&
                Objects.equals(errorMessage, order.errorMessage) &&
                // Objects.equals(inputs, order.inputs) &&
                Objects.equals(results, order.results) &&
                Objects.equals(statusLogs, order.statusLogs);
    }

    @Override
    public int hashCode() {

        return Objects.hash(externalId, externalRequest, orderOperation, orderType, status, errorMessage /*, inputs,*/, results, statusLogs);
    }
}
