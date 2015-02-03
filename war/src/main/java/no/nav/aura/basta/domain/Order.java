package no.nav.aura.basta.domain;


import com.google.common.collect.Maps;
import no.nav.aura.basta.domain.input.vm.HostnamesInput;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.domain.input.vm.OrderStatus;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "ORDERTABLE")
@SequenceGenerator(name = "hibernate_sequence", sequenceName = "order_seq", allocationSize = 1)
public class Order extends ModelEntity {

    private String externalId;
    @Lob
    private String externalRequest;
    @Enumerated(EnumType.STRING)
    private OrderType orderType;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String errorMessage;

    @ElementCollection
    @MapKeyColumn(name = "input_key")
    @Column(name = "input_value")
    @CollectionTable(name = "input_properties", joinColumns = @JoinColumn(name="order_id"))
    private Map<String, String> inputs = Maps.newHashMap();


    @ElementCollection
    @MapKeyColumn(name = "result_key")
    @Column(name = "result_value")
    @CollectionTable(name = "result_properties", joinColumns = @JoinColumn(name="order_id"))
    private Map<String, String> results = Maps.newHashMap();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId")
    private Set<OrderStatusLog> statusLogs = new HashSet<>();


    public Order(OrderType orderType, MapOperations input) {
        this.orderType = orderType;
        this.inputs = input.copy();
        this.status = OrderStatus.NEW;

    }


    private Order() {
    }


    public static Order newProvisionOrder(MapOperations input) {
        return new Order(OrderType.PROVISION, input);
    }


    public static Order newProvisionOrder(NodeType nodeType) {
        VMOrderInput input = new VMOrderInput(Maps.newHashMap());
        input.setNodeType(nodeType);

        return new Order(OrderType.PROVISION, input);
    }

    private static Order newOrderOfType(OrderType orderType, String... hostnames) {
        return new Order(orderType, new HostnamesInput(hostnames));
    }

    public static Order newDecommissionOrder(String... hostnames) {
        return newOrderOfType(OrderType.DECOMMISSION, hostnames);
    }

    public static Order newStopOrder(String... hostnames) {
        return newOrderOfType(OrderType.STOP, hostnames);
    }

    public static Order newStartOrder(String... hostnames) {
        return newOrderOfType(OrderType.START, hostnames);
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


    public <T extends MapOperations> T getInputAs(Class<T> inputClass){
        return MapOperations.as(inputClass, inputs);
    }

    public void setInput(MapOperations input) {
        this.inputs = input.copy();
    }

    public <T extends MapOperations> T getResultAs(Class<T> resultClass){
       return MapOperations.as(resultClass, results);
    }
}
