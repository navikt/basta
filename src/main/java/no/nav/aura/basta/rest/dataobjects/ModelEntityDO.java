package no.nav.aura.basta.rest.dataobjects;

import java.time.ZonedDateTime;

import com.sun.xml.txw2.annotation.XmlElement;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import no.nav.aura.basta.domain.ModelEntity;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ModelEntityDO {

    private Long id;
    private ZonedDateTime created;
    private String createdBy;
    private ZonedDateTime updated;
    private String updatedBy;
    private String updatedByDisplayName;
    private String createdByDisplayName;

    public ModelEntityDO() {
    }

    public ModelEntityDO(ModelEntity modelEntity) {
        this.id = modelEntity.getId();
        this.created = modelEntity.getCreated();
        this.createdBy = modelEntity.getCreatedBy();
        this.updated = modelEntity.getUpdated();
        this.updatedBy = modelEntity.getUpdatedBy();
        this.updatedByDisplayName = modelEntity.getUpdatedByDisplayName();
        this.createdByDisplayName = modelEntity.getCreatedByDisplayName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getCreated() {
    	return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created == null ? null : created;
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
        this.updated = updated == null ? null : updated;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedByDisplayName() {
        return updatedByDisplayName;
    }

    public void setUpdatedByDisplayName(String updatedByDisplayName) {
        this.updatedByDisplayName = updatedByDisplayName;
    }

    public String getCreatedByDisplayName() {
        return createdByDisplayName;
    }

    public void setCreatedByDisplayName(String createdByDisplayName) {
        this.createdByDisplayName = createdByDisplayName;
    }
}
