package no.nav.aura.basta.backend.fasit.deprecated.envconfig.client;

import java.util.HashSet;
import java.util.Set;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "applicationGroup")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ApplicationGroupDO {

    private String name;

    private Set<ApplicationDO> applications = new HashSet<>();

    public ApplicationGroupDO() {
    }

    public ApplicationGroupDO(String name) {
        this.name = name;
    }

    public ApplicationGroupDO(String name, Set<ApplicationDO> applications) {
        this.name = name;
        this.applications = applications;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setApplications(Set<ApplicationDO> applications) {
        this.applications = applications;
    }

    @XmlElement(name = "application")
    public Set<ApplicationDO> getApplications() {
        return applications;
   }
}
