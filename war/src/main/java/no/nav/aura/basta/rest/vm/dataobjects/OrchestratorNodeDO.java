package no.nav.aura.basta.rest.vm.dataobjects;

import java.net.URL;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm.MiddleWareType;

/**
 * Example
 *
 * <vm>
    <hostName>{vmFqdn}</hostName>
    <deployUser>deployer</deployUser>
    <deployerPassword>{deployUserPassword}</deployerPassword>
    <cpuCount>{vAppVmCpuCount}</cpuCount>
    <memoryMb>{vAppVmRamSize}</memoryMb>
    <middlewareType>{inp_vmType}</middlewareType>
    <adminUrl>{vAppPortalUrl}</adminUrl>
    <sslCert/>
    <sslPrivateKey/>
    <sslpassphrase/>
    <vApp>{vApp.name}</vApp>
    <datasenter>{inp_vmSite}</datasenter>
 </vm>;
 */
@XmlRootElement(name = "vm")
@XmlAccessorType(XmlAccessType.FIELD)
public class OrchestratorNodeDO {
    private String hostName;
    private String deployUser;
    private String deployerPassword;
    private int cpuCount;
    private int memoryMb;
    private MiddleWareType middlewareType;
    private URL adminUrl;
    private String sslCert;
    private String sslPrivateKey;
    private String sslpassphrase;
    private String vApp;
    private String datasenter;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getDeployUser() {
        return deployUser;
    }

    public void setDeployUser(String deployUser) {
        this.deployUser = deployUser;
    }

    public String getDeployerPassword() {
        return deployerPassword;
    }

    public void setDeployerPassword(String deployerPassword) {
        this.deployerPassword = deployerPassword;
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

    public MiddleWareType getMiddlewareType() {
        return middlewareType;
    }

    public void setMiddlewareType(MiddleWareType middlewareType) {
        this.middlewareType = middlewareType;
    }

    public URL getAdminUrl() {
        return adminUrl;
    }

    public void setAdminUrl(URL adminUrl) {
        this.adminUrl = adminUrl;
    }

    public String getSslCert() {
        return sslCert;
    }

    public void setSslCert(String sslCert) {
        this.sslCert = sslCert;
    }

    public String getSslPrivateKey() {
        return sslPrivateKey;
    }

    public void setSslPrivateKey(String sslPrivateKey) {
        this.sslPrivateKey = sslPrivateKey;
    }

    public String getSslpassphrase() {
        return sslpassphrase;
    }

    public void setSslpassphrase(String sslpassphrase) {
        this.sslpassphrase = sslpassphrase;
    }

    public String getvApp() {
        return vApp;
    }

    public void setvApp(String vApp) {
        this.vApp = vApp;
    }

    public String getDatasenter() {
        return datasenter;
    }

    public void setDatasenter(String datasenter) {
        this.datasenter = datasenter;
    }

}
