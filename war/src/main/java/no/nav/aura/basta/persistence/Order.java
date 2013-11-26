package no.nav.aura.basta.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "OrderTable")
public class Order {
    @Id
    @GeneratedValue
    @Column
    private Long id;
    private String orchestratorOrderId;
    private String user;
    @Lob
    private String requestXml;

    public Order() {
    }

    public Order(String orchestratorOrderId, String user, String requestXml) {
        this.orchestratorOrderId = orchestratorOrderId;
        this.user = user;
        this.requestXml = requestXml;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRequestXml() {
        return requestXml;
    }

    public void setRequestXml(String requestXml) {
        this.requestXml = requestXml;
    }

}
