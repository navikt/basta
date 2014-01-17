package no.nav.aura.basta.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.basta.vmware.orchestrator.response.OrchestratorResponse;

@XmlAccessorType(XmlAccessType.FIELD)
public class OrderResponseDO {

    private String errorMessage;
    private OrderStatus status;

    public OrderResponseDO(OrchestratorResponse status) {
        if (status == null) {
            this.status = OrderStatus.PROCESSING;
        } else {
            this.status = status.isDeploymentSuccess() ? OrderStatus.SUCCESS : OrderStatus.FAILURE;
            this.errorMessage = status.getErr();
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

}
