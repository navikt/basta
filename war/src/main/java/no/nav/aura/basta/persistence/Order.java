package no.nav.aura.basta.persistence;

import no.nav.aura.basta.rest.OrderStatus;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "orderId")
    private Set<OrderStatusLog> statusLogs = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "settings_id")
    private Settings settings;


    @SuppressWarnings("unused")
    private Order() {
    }

    public Order(NodeType nodeType) {
        this.nodeType = nodeType;
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



    public Set<OrderStatusLog> getStatusLogs(){
        return statusLogs;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }
}
