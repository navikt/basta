package no.nav.aura.basta.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "OrderTable")
public class Order {
    @Id
    @GeneratedValue
    @Column
    private Long id;
    private String orchestratorOrderId;

    public Order() {
    }

    public Order(String orchestratorOrderId) {
        this.orchestratorOrderId = orchestratorOrderId;
    }

    public String getOrchestratorOrderId() {
        return orchestratorOrderId;
    }

    public void setOrchestratorOrderId(String orchestratorOrderId) {
        this.orchestratorOrderId = orchestratorOrderId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
