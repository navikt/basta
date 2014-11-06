package no.nav.aura.basta.persistence;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import no.nav.aura.basta.Converters;
import no.nav.aura.basta.rest.OrderDetailsDO;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;

import javax.persistence.*;
import java.util.Map;

@Entity
@Table
@SequenceGenerator(name = "hibernate_sequence", sequenceName = "hibernate_sequence")
public class Settings extends ModelEntity {

    private String applicationMappingName;
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
        this.applicationMappingName = orderDetails.getApplicationMappingName();
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

    public String getApplicationMappingName() {
        return applicationMappingName;
    }

    public void setApplicationMappingName(String applicationMappingName) {
        this.applicationMappingName = applicationMappingName;
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
