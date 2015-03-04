package no.nav.aura.basta.domain;



import no.nav.aura.basta.domain.ModelEntity;

import javax.persistence.*;


@Entity
@Table
@SequenceGenerator(name = "hibernate_sequence", sequenceName = "orderstatus_seq",allocationSize = 1)
public class OrderStatusLog extends ModelEntity {


    private String statusText;
    private String statusType;
    private String statusOption;
    private String statusSource;

    @SuppressWarnings("unused")
    private OrderStatusLog(){

    }

    public OrderStatusLog(String statusSource, String statusText, String statusType, String statusOption) {

        this.statusSource = statusSource;
        this.statusText = statusText;
        this.statusType = statusType;
        this.statusOption = statusOption;
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

    public String getStatusSource() {
        return statusSource;
    }

    public void setStatusSource(String statusSource) {
        this.statusSource = statusSource;
    }
}
