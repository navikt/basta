package no.nav.aura.basta.domain;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;

@Entity
@Table(name = "SYSTEM_NOTIFICATION")
@SequenceGenerator(name = "hibernate_sequence", sequenceName = "hibernate_sequence")
public class SystemNotification extends ModelEntity {

   private String message;
   private boolean active;
   private boolean blockOperations;

    public SystemNotification(){

    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    private SystemNotification(String message, boolean active, boolean blockOperations) {
        this.message = message;
        this.active = active;
        this.blockOperations = blockOperations;
    }


    public static SystemNotification newSystemNotification(String message){
        return new SystemNotification(message,true,false);
    }

    public static SystemNotification newBlockingSystemNotification(String message){
        return new SystemNotification(message,true,true);
    }

    public void setInactive(){
        this.active = false;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    public boolean isBlockOperations() {
        return blockOperations;
    }

    public void setBlockOperations(boolean blockOperations) {
        this.blockOperations = blockOperations;
    }
}
