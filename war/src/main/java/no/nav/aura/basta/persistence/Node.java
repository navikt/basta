package no.nav.aura.basta.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class Node {

    @Id
    @GeneratedValue
    @Column
    private Long id;
    private Long orderId;
    private String hostname;

    public Node() {
    }

    public Node(Long orderId, String hostname) {
        this.orderId = orderId;
        this.hostname = hostname;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

}
