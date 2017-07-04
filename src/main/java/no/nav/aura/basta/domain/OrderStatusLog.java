package no.nav.aura.basta.domain;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;

@Entity
@Table
@SequenceGenerator(name = "hibernate_sequence", sequenceName = "orderstatus_seq", allocationSize = 1)
public class OrderStatusLog extends ModelEntity {

    private String statusText;
    private String statusType;
    private String statusOption;
    private String statusSource;

    @SuppressWarnings("unused")
    private OrderStatusLog() {

    }

    public OrderStatusLog(String text){
        this(null,text,null, StatusLogLevel.info);
    }

    public OrderStatusLog(String text, StatusLogLevel statusLogLevel){
        this(null,text,null,statusLogLevel);
    }


    public OrderStatusLog(String statusSource, String statusText, String statusType) {
        this(statusSource, statusText, statusType, StatusLogLevel.info);
    }

    public OrderStatusLog(String statusSource, String statusText, String statusType, StatusLogLevel statusOption) {
        this.statusSource = statusSource;
        this.statusText = statusText;
        this.statusType = statusType;
        setStatusOption(statusOption);
    }

    public StatusLogLevel getStatusOption() {
        return StatusLogLevel.valueOfWithDefault(statusOption);
    }

    public void setStatusOption(StatusLogLevel statusOption) {
        if (statusOption != null) {
            this.statusOption = statusOption.name();
        }
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
