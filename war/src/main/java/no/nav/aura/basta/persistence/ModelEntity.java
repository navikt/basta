package no.nav.aura.basta.persistence;

import javax.persistence.*;

import no.nav.aura.basta.User;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@MappedSuperclass
public class ModelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_sequence")
    @Column
    private Long id;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime created;

    private String createdBy;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime updated;

    private String updatedBy;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @PrePersist
    @PreUpdate
    protected void onMerge() {
        DateTime now = DateTime.now();
        String userName = User.getCurrentUser().getName();
        if (isNew()) {
            setCreated(now);
            setCreatedBy(userName);
            setUpdated(now);
            setUpdatedBy(userName);
        } else {
            setUpdated(now);
            setUpdatedBy(userName);
        }
    }

    public boolean isNew() {
        return id == null;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public DateTime getUpdated() {
        return updated;
    }

    public void setUpdated(DateTime updated) {
        this.updated = updated;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

}
