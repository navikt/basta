package no.nav.aura.basta.domain;

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

import no.nav.aura.basta.domain.input.Input;
import no.nav.aura.basta.domain.input.serviceuser.ServiceUserOrderInput;
import no.nav.aura.basta.domain.input.vm.HostnamesInput;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.domain.result.Result;
import no.nav.aura.basta.domain.result.serviceuser.ServiceUserResult;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;

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
    private Map<String, String> results = Maps.newHashMap();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId")
    private Set<OrderStatusLog> statusLogs = new HashSet<>();

    public Order(OrderType orderType, OrderOperation orderOperation, MapOperations input) {
        this.orderType = orderType;
        this.orderOperation = orderOperation;
        this.inputs = input.copy();
        this.status = OrderStatus.NEW;

    }

    private Order() {
    }

    public static Order newProvisionOrder(MapOperations input) {
        return new Order(OrderType.VM, OrderOperation.CREATE, input);
    }

    public static Order newProvisionOrderUsedOnlyForTestingPurposesRefactorLaterIPromise_yeahright(NodeType nodeType) {
        VMOrderInput input = new VMOrderInput(Maps.newHashMap());
        input.setNodeType(nodeType);
        input.addDefaultValueIfNotPresent(VMOrderInput.SERVER_COUNT, "1");
        input.addDefaultValueIfNotPresent(VMOrderInput.DISKS, "0");
        return newProvisionOrder(input);
    }

    private static Order newOrderOfType(OrderOperation orderOperation, String... hostnames) {
        return new Order(OrderType.VM, orderOperation, new HostnamesInput(hostnames));
    }

    public static Order newDecommissionOrder(String... hostnames) {
        return newOrderOfType(OrderOperation.DELETE, hostnames);
    }

    public static Order newStopOrder(String... hostnames) {
        return newOrderOfType(OrderOperation.STOP, hostnames);
    }

    public static Order newStartOrder(String... hostnames) {
        return newOrderOfType(OrderOperation.START, hostnames);
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

    public <T extends MapOperations> T getResultAs(Class<T> resultClass) {
        return MapOperations.as(resultClass, results);
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
