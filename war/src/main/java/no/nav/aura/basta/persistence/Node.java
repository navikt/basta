package no.nav.aura.basta.persistence;

import java.net.URL;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import no.nav.aura.basta.rest.SettingsDO.ApplicationServerType;

@Entity
@Table
public class Node {

    @Id
    @GeneratedValue
    @Column
    private Long id;
    private Long orderId;
    private String hostname;
    private URL adminUrl;
    private int cpuCount;
    private int memoryMb;
    private String datasenter;
    @Enumerated(EnumType.STRING)
    private ApplicationServerType applicationServerType;
    private String vapp;

    public Node() {
    }

    public Node(Long orderId, String hostname, URL adminUrl, int cpuCount, int memoryMb, String datasenter, ApplicationServerType applicationServerType, String vapp) {
        this.orderId = orderId;
        this.hostname = hostname;
        this.adminUrl = adminUrl;
        this.cpuCount = cpuCount;
        this.memoryMb = memoryMb;
        this.datasenter = datasenter;
        this.applicationServerType = applicationServerType;
        this.vapp = vapp;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ApplicationServerType getApplicationServerType() {
        return applicationServerType;
    }

    public void setApplicationServerType(ApplicationServerType applicationServerType) {
        this.applicationServerType = applicationServerType;
    }

    public String getVapp() {
        return vapp;
    }

    public void setVapp(String vapp) {
        this.vapp = vapp;
    }

}
