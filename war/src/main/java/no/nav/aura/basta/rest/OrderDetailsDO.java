package no.nav.aura.basta.rest;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.NodeType;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.persistence.OrderType;
import no.nav.aura.basta.persistence.ServerSize;
import no.nav.aura.basta.persistence.Zone;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderDetailsDO {

    private NodeType nodeType;
    private OrderType orderType;
    private Integer serverCount;
    private ServerSize serverSize;
    private String environmentName;
    private String applicationMappingName;
    private EnvironmentClass environmentClass;
    private Zone zone;
    private MiddleWareType middleWareType;
    private String commonDatasource;
    private String failoverDatasource;
    private String recoveryDatasource;
    private String cellDatasource;
    private String[] hostnames;
    private String wasAdminCredential;
    private String bpmServiceCredential;
    private String ldapUserCredential;
    private Integer disks;

    public OrderDetailsDO() {
//        applicationMapping = new ApplicationMapping();
    }

    public OrderDetailsDO(Order order) {
        this.nodeType = order.getNodeType();
        this.orderType = order.getOrderType();
        VMOrderInput input = order.getInputAs(VMOrderInput.class);
        this.serverCount = input.getServerCount();
        this.serverSize = input.getServerSize();
        this.disks = input.getDisks();
        this.environmentName = input.getEnvironmentName();
        this.applicationMappingName = input.getApplicationMappingName();
        this.environmentClass = input.getEnvironmentClass();
        this.zone = input.getZone();
        this.middleWareType = input.getMiddleWareType();
        this.cellDatasource = input.getCellDatasource();
        this.commonDatasource = input.getBpmCommonDatasource();
        this.failoverDatasource = input.getFailoverDatasource();
        this.recoveryDatasource = input.getRecoveryDataSource();
        this.wasAdminCredential = input.getWasAdminCredential();
        this.bpmServiceCredential = input.getBpmServiceCredential();
        this.ldapUserCredential =input.getLdapUserCredential();
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

    public String getApplicationMappingName() {
        return applicationMappingName;
    }

    public void setApplicationMappingName(String applicationMappingName) {
        this.applicationMappingName = applicationMappingName;
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

    public String getFailoverDatasource() {
        return failoverDatasource;
    }

    public void setFailoverDatasource(String failoverDatasource) {
        this.failoverDatasource = failoverDatasource;
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

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public String getRecoveryDatasource() {
        return recoveryDatasource;
    }

    public void setRecoveryDatasource(String recoveryDatasource) {
        this.recoveryDatasource = recoveryDatasource;
    }
}
