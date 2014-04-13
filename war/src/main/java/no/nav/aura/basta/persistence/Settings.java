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

import no.nav.aura.basta.Converters;
import no.nav.aura.basta.rest.OrderDetailsDO;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

@Entity
@Table
public class Settings extends ModelEntity {

    @ManyToOne(cascade = CascadeType.MERGE)
    private Order order;
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

    private Integer disks;

    @ElementCollection
    @MapKeyColumn(name = "property_key")
    @Column(name = "property_value")
    @CollectionTable(name = "settings_properties")
    private Map<String, String> properties = Maps.newHashMap();

    private Boolean xmlCustomized;

    @SuppressWarnings("unused")
    private Settings() {
    }

    public Settings(Order order) {
        this.order = order;
    }

    public Settings(Order order, OrderDetailsDO orderDetails) {
        this(order);
        this.applicationName = orderDetails.getApplicationName();
        this.middleWareType = orderDetails.getMiddleWareType();
        this.environmentClass = orderDetails.getEnvironmentClass();
        this.environmentName = orderDetails.getEnvironmentName();
        this.serverCount = orderDetails.getServerCount();
        this.serverSize = orderDetails.getServerSize();
        this.zone = orderDetails.getZone();
        this.disks = orderDetails.getDisks();
        FasitProperties.apply(orderDetails, this);
        DecommissionProperties.apply(orderDetails, this);
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


    public boolean isMultisite() {
        return Converters.isMultisite(environmentClass,environmentName);
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public Optional<String> getProperty(String key) {
        return Optional.fromNullable(properties.get(key));
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
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

    public void addDisk(){
        if (Optional.fromNullable(disks).isPresent()) {
            disks++;
        }else{
            disks = 1;
        }
    }
}
