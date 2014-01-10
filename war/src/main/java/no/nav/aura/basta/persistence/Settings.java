package no.nav.aura.basta.persistence;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

import no.nav.aura.basta.rest.OrderDetailsDO;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

@Entity
@Table
public class Settings extends ModelEntity {

    @ManyToOne(cascade = CascadeType.MERGE)
    private Order order;
    @Enumerated(EnumType.STRING)
    private NodeType nodeType;
    private String applicationName;
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
    private boolean disk;

    @ElementCollection
    @MapKeyColumn(name = "property_key")
    @Column(name = "property_value")
    @CollectionTable(name = "settings_properties")
    private Map<String, String> properties = Maps.newHashMap();

    public Settings() {
    }

    public Settings(Order order, OrderDetailsDO settings) {
        this.nodeType = settings.getNodeType();
        this.order = order;
        this.applicationName = settings.getApplicationName();
        this.middleWareType = settings.getMiddleWareType();
        this.environmentClass = settings.getEnvironmentClass();
        this.environmentName = settings.getEnvironmentName();
        this.serverCount = settings.getServerCount();
        this.serverSize = settings.getServerSize();
        this.zone = settings.getZone();
        this.disk = settings.isDisk();
        BpmProperties.apply(settings, this);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
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

    public boolean isDisk() {
        return disk;
    }

    public void setDisk(boolean disk) {
        this.disk = disk;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public boolean isMultisite() {
        switch (environmentClass) {
        case p:
            return true;
        case q:
            return environmentName.matches("q[013]");
        default:
            return false;
        }
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public Optional<String> getProperty(String key) {
        return Optional.fromNullable(properties.get(key));
    }
}
