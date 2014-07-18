package no.nav.aura.basta.rest;

import com.sun.xml.txw2.annotation.XmlElement;
import no.nav.aura.basta.persistence.*;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Arrays;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderDetailsDO {

    private NodeType nodeType;
    private Integer serverCount;
    private ServerSize serverSize;
    private String environmentName;
    //Todo: Rename Application to applicationMapping or something better that will reflect that this field can contain both application name and applicationgroupname
    private Application applicationName;
    private EnvironmentClass environmentClass;
    private Zone zone;
    private MiddleWareType middleWareType;
    private String commonDatasource;
    private String cellDatasource;
    private String[] hostnames;
    private String wasAdminCredential;
    private String bpmServiceCredential;
    private String ldapUserCredential;
    private Integer disks;

    public OrderDetailsDO() {
    }

    public OrderDetailsDO(Settings settings) {
        this.nodeType = settings.getOrder().getNodeType();
        this.serverCount = settings.getServerCount();
        this.serverSize = settings.getServerSize();
        this.disks = settings.getDisks();
        this.environmentName = settings.getEnvironmentName();
        this.applicationName = new Application(settings.getApplicationName());
        this.environmentClass = settings.getEnvironmentClass();
        this.zone = settings.getZone();
        this.middleWareType = settings.getMiddleWareType();
        FasitProperties.apply(settings, this);
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

    public Application getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(Application application) {
        this.applicationName = application;
    }

    public EnvironmentClass getEnvironmentClass() {
        return environmentClass;
    }

    public void setEnvironmentClass(EnvironmentClass environmentClass) {
        this.environmentClass = environmentClass;
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

    public String[] getHostnames() {
        return hostnames == null ? null : Arrays.copyOf(hostnames, hostnames.length);
    }

    public void setHostnames(String[] hostnames) {
        this.hostnames = hostnames;
    }

    public MiddleWareType getMiddleWareType() {
        return middleWareType;
    }

    public void setMiddleWareType(MiddleWareType middleWareType) {
        this.middleWareType = middleWareType;
    }

    public String getWasAdminCredential() {
        return wasAdminCredential;
    }

    public void setWasAdminCredential(String wasAdminCredential) {
        this.wasAdminCredential = wasAdminCredential;
    }

    public String getBpmServiceCredential() {
        return bpmServiceCredential;
    }

    public void setBpmServiceCredential(String bpmServiceCredential) {
        this.bpmServiceCredential = bpmServiceCredential;
    }

    public Integer getDisks() {
        return disks;
    }

    public void setDisks(Integer disks) {
        this.disks = disks;
    }

    public String getLdapUserCredential() {
        return ldapUserCredential;
    }

    public void setLdapUserCredential(String ldapUserCredential) {
        this.ldapUserCredential = ldapUserCredential;
    }
}
