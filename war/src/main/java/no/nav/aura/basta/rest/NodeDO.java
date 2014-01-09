package no.nav.aura.basta.rest;

import java.net.URL;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeDO {

    private URL adminUrl;
    private MiddleWareType middleWareType;
    private int cpuCount;
    private String datasenter;
    private String hostname;
    private int memoryMb;
    private String vapp;

    public NodeDO(URL adminUrl, MiddleWareType applicationServerType, int cpuCount, String datasenter, String hostname, int memoryMb, String vapp) {
        this.adminUrl = adminUrl;
        this.middleWareType = applicationServerType;
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

    public MiddleWareType getMiddleWareType() {
        return middleWareType;
    }

    public void setMiddleWareType(MiddleWareType middleWareType) {
        this.middleWareType = middleWareType;
    }

    public URL getAdminUrl() {
        return adminUrl;
    }

    public void setAdminUrl(URL adminUrl) {
        this.adminUrl = adminUrl;
    }

}
