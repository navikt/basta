package no.nav.aura.basta.persistence;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;

import no.nav.aura.basta.rest.OrderStatus;

@Entity
@Table(name = "OrderTable")
public class Order extends ModelEntity {

    private String orchestratorOrderId;
    @Lob
    private String requestXml;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String errorMessage;

    public Order() {
        status = OrderStatus.NEW;
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

}
