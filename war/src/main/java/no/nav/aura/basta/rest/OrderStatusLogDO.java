package no.nav.aura.basta.rest;

import no.nav.aura.basta.persistence.OrderStatusLog;
import org.joda.time.DateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 '<status>
 <text>System is registered in Satellite</text>
 <type>puppetverify:ok</type>
 <option/>
 </status>'
 */
@XmlRootElement(name = "status")
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderStatusLogDO  extends ModelEntityDO{

    private String text;
    private String type;
    private String option;

    public OrderStatusLogDO(){
        super();
    }

    public OrderStatusLogDO (OrderStatusLog orderStatusLog){
        super(orderStatusLog);
        this.text = orderStatusLog.getStatusText();
        this.type = orderStatusLog.getStatusType();
        this.option = orderStatusLog.getStatusOption();
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

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}
