package no.nav.aura.basta.rest;

import java.net.URL;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.basta.persistence.ApplicationServerType;


import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeDO {

    private URL adminUrl;
    private ApplicationServerType applicationServerType;
    private int cpuCount;
    private String datasenter;
    private String hostname;
    private int memoryMb;
    private String vapp;

    public NodeDO(URL adminUrl, ApplicationServerType applicationServerType, int cpuCount, String datasenter, String hostname, int memoryMb, String vapp) {
        this.adminUrl = adminUrl;
        this.applicationServerType = applicationServerType;
        this.cpuCount = cpuCount;
        this.datasenter = datasenter;
        this.hostname = hostname;
        this.memoryMb = memoryMb;
        this.vapp = vapp;
    }

    public String getVapp() {
        return vapp;
    }

    public void setVapp(String vapp) {
        this.vapp = vapp;
    }

    public int getMemoryMb() {
        return memoryMb;
    }

    public void setMemoryMb(int memoryMb) {
        this.memoryMb = memoryMb;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getDatasenter() {
        return datasenter;
    }

    public void setDatasenter(String datasenter) {
        this.datasenter = datasenter;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public ApplicationServerType getApplicationServerType() {
        return applicationServerType;
    }

    public void setApplicationServerType(ApplicationServerType applicationServerType) {
        this.applicationServerType = applicationServerType;
    }

    public URL getAdminUrl() {
        return adminUrl;
    }

    public void setAdminUrl(URL adminUrl) {
        this.adminUrl = adminUrl;
    }

}
