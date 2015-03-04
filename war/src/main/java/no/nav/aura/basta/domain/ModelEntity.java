package no.nav.aura.basta.domain;

import javax.persistence.*;

import no.nav.aura.basta.security.User;

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
    private String createdByDisplayName;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime updated;

    private String updatedBy;
    private String updatedByDisplayName;


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
        String userDisplayName = User.getCurrentUser().getDisplayName();
        if (isNew()) {
            setCreated(now);
            setCreatedBy(userName);
            setCreatedByDisplayName(userDisplayName);
            setUpdated(now);
            setUpdatedBy(userName);
            setUpdatedByDisplayName(userDisplayName);
        } else {
            setUpdated(now);
            setUpdatedBy(userName);
            setUpdatedByDisplayName(userDisplayName);
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

    public String getCreatedByDisplayName() {
        return createdByDisplayName;
    }

    public void setCreatedByDisplayName(String createdByDisplayName) {
        this.createdByDisplayName = createdByDisplayName;
    }

    public String getUpdatedByDisplayName() {
        return updatedByDisplayName;
    }

    public void setUpdatedByDisplayName(String updatedByDisplayName) {
        this.updatedByDisplayName = updatedByDisplayName;
    }
}
