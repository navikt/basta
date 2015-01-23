package no.nav.aura.basta.domain;


import com.google.common.collect.Maps;
import no.nav.aura.basta.domain.vminput.HostnamesInput;
import no.nav.aura.basta.domain.vminput.NodeTypeInputResolver;
import no.nav.aura.basta.persistence.*;
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
    private String requestXml;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(name = "ORDER_NODE", joinColumns = {@JoinColumn(name = "order_id")}, inverseJoinColumns = {@JoinColumn(name = "node_id")})
    private Set<Node> nodes = new HashSet<>();
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
        this.nodeType = NodeTypeInputResolver.getNodeType(input);
        this.status = OrderStatus.NEW;

    }


    private Order() {
    }


    public static Order newProvisionOrder(Input input) {
        return new Order(OrderType.PROVISION, input);
    }


    public static Order newProvisionOrder(NodeType nodeType) {
        return new Order(OrderType.PROVISION, NodeTypeInputResolver.asInput(nodeType));
    }

    private static Order newOrderOfType(OrderType orderType, String... hostnames) {
        return new Order(orderType, HostnamesInput.asInput(hostnames));
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


    public String getRequestXml() {
        return requestXml;
    }

    public void setRequestXml(String requestXml) {
        this.requestXml = requestXml;
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
                       ", requestXml='" + requestXml + '\'' +
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


    public Node addNode(Node node) {
        nodes.add(node);
        return node;
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    @Deprecated
    public Input getInput() {
        return new Input(input_properties);
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
}
