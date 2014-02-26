package no.nav.aura.basta.rest;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.basta.persistence.ModelEntity;

import org.joda.time.DateTime;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ModelEntityDO {

    private Long id;
    private DateTime created;
    private String createdBy;
    private DateTime updated;
    private String updatedBy;

    public ModelEntityDO() {
    }

    public ModelEntityDO(ModelEntity modelEntity) {
        this.id = modelEntity.getId();
        this.created = modelEntity.getCreated();
        this.createdBy = modelEntity.getCreatedBy();
        this.updated = modelEntity.getUpdated();
        this.updatedBy = modelEntity.getUpdatedBy();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreated() {
        return created == null ? null : created.toDate();
    }

    public void setCreated(Date created) {
        this.created = created == null ? null : new DateTime(created.getTime());
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdated() {
        return updated == null ? null : updated.toDate();
    }

    public void setUpdated(Date updated) {
        this.updated = updated == null ? null : new DateTime(updated.getTime());
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

}
