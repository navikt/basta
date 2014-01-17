package no.nav.aura.basta.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.basta.persistence.BpmProperties;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.NodeType;
import no.nav.aura.basta.persistence.ServerSize;
import no.nav.aura.basta.persistence.Settings;
import no.nav.aura.basta.persistence.Zone;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderDetailsDO {

    private NodeType nodeType;
    private Integer serverCount;
    private ServerSize serverSize;
    private boolean disk;
    private String environmentName;
    private String applicationName;
    private EnvironmentClass environmentClass;
    private Zone zone;
    private MiddleWareType middleWareType;
    private String commonDatasource;
    private String cellDatasource;

    public OrderDetailsDO() {
    }

    public OrderDetailsDO(Settings settings) {
        this.nodeType = settings.getOrder().getNodeType();
        this.serverCount = settings.getServerCount();
        this.serverSize = settings.getServerSize();
        this.disk = settings.isDisk();
        this.environmentName = settings.getEnvironmentName();
        this.applicationName = settings.getApplicationName();
        this.environmentClass = settings.getEnvironmentClass();
        this.zone = settings.getZone();
        this.middleWareType = settings.getMiddleWareType();
        BpmProperties.apply(settings, this);
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

    public boolean isDisk() {
        return disk;
    }

    public void setDisk(boolean disk) {
        this.disk = disk;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String application) {
        this.applicationName = application;
    }

    public EnvironmentClass getEnvironmentClass() {
        return environmentClass;
    }

    public void setEnvironmentClass(EnvironmentClass environmentClass) {
        this.environmentClass = environmentClass;
    }

    public MiddleWareType getMiddleWareType() {
        return middleWareType;
    }

    public void setApplicationServerType(MiddleWareType middleWareType) {
        this.middleWareType = middleWareType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public String getCommonDatasource() {
        return commonDatasource;
    }

    public void setCommonDatasource(String commonDatasource) {
        this.commonDatasource = commonDatasource;
    }

    public String getCellDatasource() {
        return cellDatasource;
    }

    public void setCellDatasource(String cellDatasource) {
        this.cellDatasource = cellDatasource;
    }

}
