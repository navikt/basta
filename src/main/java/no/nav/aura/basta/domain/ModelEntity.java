package no.nav.aura.basta.domain;

import jakarta.persistence.*;

import no.nav.aura.basta.security.User;

import java.time.ZonedDateTime;

@MappedSuperclass
public class ModelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_sequence")
    @Column
    private Long id;

    private ZonedDateTime created;

    private String createdBy;
    private String createdByDisplayName;

    private ZonedDateTime updated;

    private String updatedBy;
    private String updatedByDisplayName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @PrePersist
    protected void onCreate() {
    	ZonedDateTime now = ZonedDateTime.now();
        String user = User.getCurrentUser().getName();
        String displayName = User.getCurrentUser().getDisplayName();
        
        setCreated(now);
        setCreatedBy(user);
        setCreatedByDisplayName(displayName);
        setUpdated(now);
        setUpdatedBy(user);
        setUpdatedByDisplayName(displayName);
    }

    @PreUpdate
    protected void onUpdate() {
    	ZonedDateTime now = ZonedDateTime.now();
        String user = User.getCurrentUser().getName();
        String displayName = User.getCurrentUser().getDisplayName();
        
        setUpdated(now);
        setUpdatedBy(user);
        setUpdatedByDisplayName(displayName);
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(ZonedDateTime updated) {
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
