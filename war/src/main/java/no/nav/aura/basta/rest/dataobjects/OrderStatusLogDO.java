package no.nav.aura.basta.rest.dataobjects;

import no.nav.aura.basta.domain.OrderStatusLog;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * '<status> <text>System is registered in Satellite</text> <type>puppetverify:ok</type> <option/> </status>'
 */
@XmlRootElement(name = "status")
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderStatusLogDO extends ModelEntityDO {

    private String text;
    private String type;
    private StatusLogLevel option;
    private String source;

    public OrderStatusLogDO() {
        super();
    }

    public OrderStatusLogDO(OrderStatusLog orderStatusLog) {
        super(orderStatusLog);
        this.text = orderStatusLog.getStatusText();
        this.type = orderStatusLog.getStatusType();
        this.option = orderStatusLog.getStatusOption();
        this.source = orderStatusLog.getStatusSource();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public StatusLogLevel getOption() {
        return option;
    }

    public void setOption(StatusLogLevel option) {
        this.option = option;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("text", text)
                .append("type", type)
                .append("option", option)
                .append("source", source)
                .toString();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
