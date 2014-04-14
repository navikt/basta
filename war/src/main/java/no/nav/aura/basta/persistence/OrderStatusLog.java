package no.nav.aura.basta.persistence;



import javax.persistence.*;

@Entity
@Table
public class OrderStatusLog extends ModelEntity {

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "orderId")
    private Order order;

    private String statusText;
    private String statusType;
    private String statusOption;

    @SuppressWarnings("unused")
    private OrderStatusLog(){

    }
    public OrderStatusLog(Order order) {
        this.order = order;
    }

    public OrderStatusLog(Order order, String statusText, String statusType, String statusOption) {
        this(order);
        this.statusText = statusText;
        this.statusType = statusType;
        this.statusOption = statusOption;
    }



    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getStatusOption() {
        return statusOption;
    }

    public void setStatusOption(String statusOption) {
        this.statusOption = statusOption;
    }

    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
}
