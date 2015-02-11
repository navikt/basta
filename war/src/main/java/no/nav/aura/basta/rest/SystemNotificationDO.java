package no.nav.aura.basta.rest;

import no.nav.aura.basta.persistence.*;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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


    public static SystemNotificationDO fromDomain(SystemNotification systemNotification){
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
