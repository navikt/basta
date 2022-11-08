package no.nav.aura.basta.rest.dataobjects;

import com.sun.xml.txw2.annotation.XmlElement;
import no.nav.aura.basta.domain.ModelEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

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

    public Date getCreated() {
        return created == null ? null : Date.from(created.toInstant());
    }

    public void setCreated(Date created) {
        this.created = created == null ? null : ZonedDateTime.ofInstant(created.toInstant(), ZoneId.systemDefault());
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdated() {
        return updated == null ? null : Date.from(updated.toInstant());
    }

    public void setUpdated(Date updated) {
        this.updated = updated == null ? null : ZonedDateTime.ofInstant(updated.toInstant(), ZoneId.systemDefault());
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
