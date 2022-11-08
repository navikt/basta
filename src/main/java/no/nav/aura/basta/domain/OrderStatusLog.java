package no.nav.aura.basta.domain;

import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table
@SequenceGenerator(name = "hibernate_sequence", sequenceName = "orderstatus_seq", allocationSize = 1)
public class OrderStatusLog extends ModelEntity {

    private String statusText;
    private String statusType;
    private String statusOption;
    private String statusSource;

    @SuppressWarnings("unused")
    protected OrderStatusLog() {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderStatusLog that = (OrderStatusLog) o;
        return Objects.equals(statusText, that.statusText) &&
                Objects.equals(statusType, that.statusType) &&
                Objects.equals(statusOption, that.statusOption) &&
                Objects.equals(statusSource, that.statusSource);
    }

    @Override
    public int hashCode() {

        return Objects.hash(statusText, statusType, statusOption, statusSource);
    }
}
