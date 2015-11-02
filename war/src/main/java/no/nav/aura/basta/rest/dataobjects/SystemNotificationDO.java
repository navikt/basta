package no.nav.aura.basta.rest.dataobjects;

import no.nav.aura.basta.domain.SystemNotification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class SystemNotificationDO extends ModelEntityDO {

    private String message;
    private boolean active;
    private boolean blockOperations;


    public SystemNotificationDO() {
        super();
    }

    private SystemNotificationDO(SystemNotification systemNotification){
        super(systemNotification);
        this.message = systemNotification.getMessage();
        this.active = systemNotification.isActive();
        this.blockOperations = systemNotification.isBlockOperations();
    }


    public static SystemNotificationDO from(SystemNotification systemNotification){
        return new SystemNotificationDO(systemNotification);

    }


    public boolean isBlockOperations() {
        return blockOperations;
    }

    public void setBlockOperations(boolean blockOperations) {
        this.blockOperations = blockOperations;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
