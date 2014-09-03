package no.nav.aura.basta.persistence;

import java.util.List;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MapKeyColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import no.nav.aura.basta.Converters;
import no.nav.aura.basta.rest.ApplicationMapping;
import no.nav.aura.basta.rest.OrderDetailsDO;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Entity
@Table
@SequenceGenerator(name = "hibernate_sequence", sequenceName = "hibernate_sequence")
public class Settings extends ModelEntity {

    private String applicationMappingName;
    @Enumerated(EnumType.STRING)
    private ApplicationMappingType mappingType = ApplicationMappingType.APPLICATION;
    @Enumerated(EnumType.STRING)
    private MiddleWareType middleWareType;
    @Enumerated(EnumType.STRING)
    private EnvironmentClass environmentClass;
    private String environmentName;
    private Integer serverCount;
    @Enumerated(EnumType.STRING)
    private ServerSize serverSize;
    @Enumerated(EnumType.STRING)
    private Zone zone;

    // We do not want to store list of application in application group, as this may change.
    // When we have the application group name, we can get the a fresh list of applications from Fasit.
    @Transient
    private List<String> appsInAppGroup = Lists.newArrayList();

    private Integer disks;

    @ElementCollection
    @MapKeyColumn(name = "property_key")
    @Column(name = "property_value")
    @CollectionTable(name = "settings_properties")
    private Map<String, String> properties = Maps.newHashMap();

    private Boolean xmlCustomized;

    public Settings() {
    }

    public Settings(OrderDetailsDO orderDetails) {
        ApplicationMapping applicationMapping = orderDetails.getApplicationMapping();
        this.applicationMappingName = applicationMapping.getName();
        this.appsInAppGroup = applicationMapping.getApplications();
        this.mappingType = applicationMapping.getMappingType();
        this.middleWareType = orderDetails.getMiddleWareType();
        this.environmentClass = orderDetails.getEnvironmentClass();
        this.environmentName = orderDetails.getEnvironmentName();
        this.serverCount = orderDetails.getServerCount();
        this.serverSize = orderDetails.getServerSize();
        this.zone = orderDetails.getZone();
        this.disks = orderDetails.getDisks();
        FasitProperties.apply(orderDetails, this);
        Hostnames.apply(orderDetails.getHostnames(), this);
    }

    public String[] getHostNames() {
        return Hostnames.extractHostnames(this).toArray(String.class);
    }

    public void setHostNames(String... hostNames) {
        Hostnames.apply(hostNames, this);
    }

    public ApplicationMapping getApplicationMapping() {
        if (mappingType.equals(ApplicationMappingType.APPLICATION_GROUP)) {
            return new ApplicationMapping(applicationMappingName, appsInAppGroup);
        }
        else {
            return new ApplicationMapping(applicationMappingName);
        }
    }

    public void setApplicationMappingName(String applicationMappingName) {
        this.applicationMappingName = applicationMappingName;
    }

    public ApplicationMappingType getMappingType() {
        return mappingType;
    }

    public void setMappingType(ApplicationMappingType mappingType) {
        this.mappingType = mappingType;
    }

    public MiddleWareType getMiddleWareType() {
        return middleWareType;
    }

    public void setMiddleWareType(MiddleWareType middleWareType) {
        this.middleWareType = middleWareType;
    }

    public EnvironmentClass getEnvironmentClass() {
        return environmentClass;
    }

    public void setEnvironmentClass(EnvironmentClass environmentClass) {
        this.environmentClass = environmentClass;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public Integer getServerCount() {
        return serverCount;
    }

    public void setServerCount(Integer serverCount) {
        this.serverCount = serverCount;
    }

    public ServerSize getServerSize() {
        return serverSize;
    }

    public void setServerSize(ServerSize serverSize) {
        this.serverSize = serverSize;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public boolean isMultisite() {
        return Converters.isMultisite(environmentClass, environmentName);
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public Optional<String> getProperty(String key) {
        return Optional.fromNullable(properties.get(key));
    }

    public void setXmlCustomized() {
        this.xmlCustomized = true;
    }

    public Boolean isXmlCustomized() {
        return xmlCustomized;
    }

    public Integer getDisks() {
        return disks;
    }

    public void setDisks(Integer disks) {
        this.disks = disks;
    }

    public void addDisk() {
        if (Optional.fromNullable(disks).isPresent()) {
            disks++;
        } else {
            disks = 1;
        }
    }
}
