package no.nav.aura.basta.domain;


import com.google.common.collect.Maps;
import no.nav.aura.basta.domain.input.Input;
import no.nav.aura.basta.domain.input.vm.HostnamesInput;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.domain.result.MapOperations;
import no.nav.aura.basta.rest.OrderStatus;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "ORDERTABLE")
@SequenceGenerator(name = "hibernate_sequence", sequenceName = "order_seq", allocationSize = 1)
public class Order extends ModelEntity {


    @ElementCollection
    @MapKeyColumn(name = "input_key")
    @Column(name = "input_value")
    @CollectionTable(name = "input_properties", joinColumns = @JoinColumn(name="order_id"))
    private Map<String, String> input_properties = Maps.newHashMap();


    private String externalId;

    @Lob
    private String externalRequest;


    @ElementCollection
    @MapKeyColumn(name = "result_key")
    @Column(name = "result_value")
    @CollectionTable(name = "result_properties", joinColumns = @JoinColumn(name="order_id"))
    private Map<String, String> result_properties = Maps.newHashMap();


    //@ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    //@JoinTable(name = "ORDER_NODE", joinColumns = {@JoinColumn(name = "order_id")}, inverseJoinColumns = {@JoinColumn(name = "node_id")})
    //private Set<Node> nodes = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private OrderType orderType;
    @Enumerated(EnumType.STRING)
    private NodeType nodeType;

    //GENERELT
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String errorMessage;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId")
    private Set<OrderStatusLog> statusLogs = new HashSet<>();


    public Order(OrderType orderType, Input input) {
        this.orderType = orderType;
        this.input_properties = input.copy();
        this.nodeType = getInputAs(VMOrderInput.class).getNodeType(); //TODO
        this.status = OrderStatus.NEW;

    }


    private Order() {
    }


    public static Order newProvisionOrder(Input input) {
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

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public String toString() {
        return "Order{" +
                       "externalId='" + externalId + '\'' +
                       ", externalRequest='" + externalRequest + '\'' +
                       ", status=" + status +
                       ", errorMessage='" + errorMessage + '\'' +
                       ", nodeType=" + nodeType +
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


    public <T extends Input> T getInputAs(Class<T> inputClass){
        try {
            return inputClass.getConstructor(Map.class).newInstance(input_properties);
        } catch (Exception e) {
            //All sorts of hell can break loose
            throw new RuntimeException(e);
        }
    }

    public void setInput(Input input) {
        this.input_properties = input.copy();
    }

    public <T extends MapOperations> T getResultAs(Class<T> resultClass){
        try {
            return resultClass.getConstructor(Map.class).newInstance(result_properties);
        } catch (Exception e) {
            //All sorts of hell can break loose
            throw new RuntimeException(e);
        }
    }
}
