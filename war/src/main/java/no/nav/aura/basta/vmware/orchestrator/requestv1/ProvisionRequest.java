package no.nav.aura.basta.vmware.orchestrator.requestv1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import no.nav.aura.basta.vmware.orchestrator.request.OrchestatorRequest;

@XmlRootElement
@XmlType(propOrder = { "environmentId", "zone", "owner", "orderedBy", "description", "portfolio", "environmentClass", "application",
        "projectId", "expires", "createApplication", "changeDeployUser", "updateEnvConf", "envConfTestEnv", "puppetEnv", "satelliteEnv", "engineeringFileList", "overrideMaintenance", "vApps" })
public class ProvisionRequest implements OrchestatorRequest {
    private String environmentId;
    private String zone;
    private String owner;
    private String orderedBy;
    private String description; // Optional
    private String portfolio;
    private String environmentClass;
    private String application;
    private String projectId;
    private String expires; // Optional Defaults to 1 year
    private boolean createApplication; // Optional Default false
    private boolean changeDeployUser; // Optional Default False
    private boolean updateEnvConf;
    private boolean envConfTestEnv; // Optional Default false
    private String puppetEnv; // Optional
    private String satelliteEnv; // Optional
    private boolean engineeringFileList; // Optional
    private boolean overrideMaintenance;

    private List<VApp> vApps;

    public boolean isCreateApplication() {
        return createApplication;
    }

    public boolean isOverrideMaintenance() {
        return overrideMaintenance;
    }

    public void setCreateApplication(boolean createApplication) {
        this.createApplication = createApplication;
    }

    public boolean isChangeDeployUser() {
        return changeDeployUser;
    }

    public void setChangeDeployUser(boolean changeDeployUser) {
        this.changeDeployUser = changeDeployUser;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

    public String getEnvironmentClass() {
        return environmentClass;
    }

    public void setEnvironmentClass(String environmentClass) {
        this.environmentClass = environmentClass;
    }

    public String getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(String orderedBy) {
        this.orderedBy = orderedBy;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public boolean isUpdateEnvConf() {
        return updateEnvConf;
    }

    public void setUpdateEnvConf(boolean updateEnvConf) {
        this.updateEnvConf = updateEnvConf;
    }

    public void setEnvConfTestEnv(boolean envConfTestEnv) {
        this.envConfTestEnv = envConfTestEnv;
    }

    public boolean isEnvConfTestEnv() {
        return this.envConfTestEnv;
    }

    public String getPuppetEnv() {
        return puppetEnv;
    }

    public void setPuppetEnv(String puppetEnv) {
        this.puppetEnv = puppetEnv;
    }

    public String getSatelliteEnv() {
        return satelliteEnv;
    }

    public void setSatelliteEnv(String satelliteEnv) {
        this.satelliteEnv = satelliteEnv;
    }

    public boolean isEngineeringFileList() {
        return engineeringFileList;
    }

    public void setEngineeringFileList(boolean engineeringFileList) {
        this.engineeringFileList = engineeringFileList;
    }

    public void setvApps(List<VApp> vApps) {
        this.vApps = vApps;
    }

    @XmlElementWrapper(name = "vapps")
    @XmlElement(name = "vapp", required = true)
    public List<VApp> getvApps() {
        return vApps;
    }

    public void addVapp(VApp vApp) {
        if (this.vApps == null) {
            this.vApps = new ArrayList<VApp>();
        }
        vApps.add(vApp);
    }

    public void setOverrideMaintenance(boolean overrideMaintenance) {
        this.overrideMaintenance = overrideMaintenance;

    }
}
