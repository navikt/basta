package no.nav.aura.basta.persistence;

import java.net.URL;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;

@Entity
@Table
public class Node extends ModelEntity {

    private Long orderId;
    private String hostname;
    private URL adminUrl;
    private int cpuCount;
    private int memoryMb;
    private String datasenter;
    @Enumerated(EnumType.STRING)
    private MiddleWareType middleWareType;
    private String vapp;
    private boolean fasitUpdated = false;

    public Node() {
    }

    public Node(Long orderId, String hostname, URL adminUrl, int cpuCount, int memoryMb, String datasenter, MiddleWareType middleWareType, String vapp) {
        this.orderId = orderId;
        this.hostname = hostname;
        this.adminUrl = adminUrl;
        this.cpuCount = cpuCount;
        this.memoryMb = memoryMb;
        this.datasenter = datasenter;
        this.middleWareType = middleWareType;
        this.vapp = vapp;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public URL getAdminUrl() {
        return adminUrl;
    }

    public void setAdminUrl(URL adminUrl) {
        this.adminUrl = adminUrl;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public int getMemoryMb() {
        return memoryMb;
    }

    public void setMemoryMb(int memoryMb) {
        this.memoryMb = memoryMb;
    }

    public String getDatasenter() {
        return datasenter;
    }

    public void setDatasenter(String datasenter) {
        this.datasenter = datasenter;
    }

    public MiddleWareType getMiddleWareType() {
        return middleWareType;
    }

    public void setMiddleWareType(MiddleWareType middleWareType) {
        this.middleWareType = middleWareType;
    }

    public String getVapp() {
        return vapp;
    }

    public void setVapp(String vapp) {
        this.vapp = vapp;
    }

    public boolean isFasitUpdated() {
        return fasitUpdated;
    }

    public void setFasitUpdated(boolean fasitUpdated) {
        this.fasitUpdated = fasitUpdated;
    }

}
