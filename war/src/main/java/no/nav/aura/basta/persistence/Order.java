package no.nav.aura.basta.persistence;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import no.nav.aura.basta.rest.OrderStatus;

@Entity
@Table(name = "OrderTable")
@SequenceGenerator(name = "hibernate_sequence", sequenceName = "order_seq", allocationSize = 1)
public class Order extends ModelEntity {

    private String orchestratorOrderId;
    @Lob
    private String requestXml;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String errorMessage;
    @Enumerated(EnumType.STRING)
    private NodeType nodeType;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId")
    private Set<OrderStatusLog> statusLogs = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "settings_id")
    private Settings settings;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(name = "ORDER_NODE", joinColumns = { @JoinColumn(name = "order_id") }, inverseJoinColumns = { @JoinColumn(name = "node_id") })
    private Set<Node> nodes = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    private Order() {
    }

    public static Order newProvisionOrder(NodeType nodeType, Settings settings) {
        return new Order(OrderType.PROVISION, nodeType, settings);
    }

    public static Order newProvisionOrder(NodeType nodeType) {
        return new Order(OrderType.PROVISION, nodeType, null);
    }

    private static Order newOrderOfType(OrderType orderType, String... hostnames) {
        Settings settings = new Settings();
        settings.setHostNames(hostnames);
        return new Order(orderType, null, settings);
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

    private Order(OrderType orderType, NodeType nodeType, Settings settings) {
        this.orderType = orderType;
        this.nodeType = nodeType;
        this.settings = settings;
        this.status = OrderStatus.NEW;
    }

    public String getOrchestratorOrderId() {
        return orchestratorOrderId;
    }

    public void setOrchestratorOrderId(String orchestratorOrderId) {
        this.orchestratorOrderId = orchestratorOrderId;
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
                "orchestratorOrderId='" + orchestratorOrderId + '\'' +
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

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
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

}
