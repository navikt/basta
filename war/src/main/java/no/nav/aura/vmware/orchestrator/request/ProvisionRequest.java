package no.nav.aura.vmware.orchestrator.request;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProvisionRequest implements OrchestatorRequest {

    public enum Zone {
        sbs, fss
    };

    public enum envClass {
        utv, test, preprod, prod
    };

    public enum Role {
        was, bpm, div
    }

    private String environmentId;
    private Zone zone;
    private String owner; // remove
    private String orderedBy;
    private envClass environmentClass;
    private String application;
    private boolean changeDeployUserPassword = false; // Optional Default False
    private Role role;// was|bpm|div</role> <!-- orchName: vAppRole, info: Part of the logic to choose the right org-vdc -->

    private URI statusCallbackUrl;
    private URI resultCallbackUrl;
    private boolean engineeringBuild = false;

    @XmlElementWrapper(name = "vapps")
    @XmlElement(name = "vapp", required = true)
    private List<VApp> vApps = new ArrayList<>();

    public String getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(String orderedBy) {
        this.orderedBy = orderedBy;
    }

    public envClass getEnvironmentClass() {
        return environmentClass;
    }

    public void setEnvironmentClass(envClass environmentClass) {
        this.environmentClass = environmentClass;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public List<VApp> getvApps() {
        return vApps;
    }

    public void setvApps(List<VApp> vApps) {
        this.vApps = vApps;
    }

    public boolean isChangeDeployUserPassword() {
        return changeDeployUserPassword;
    }

    public void setChangeDeployUserPassword(boolean changeDeployUserPassword) {
        this.changeDeployUserPassword = changeDeployUserPassword;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public URI getStatusCallbackUrl() {
        return statusCallbackUrl;
    }

    public void setStatusCallbackUrl(URI status_callback_url) {
        this.statusCallbackUrl = status_callback_url;
    }

    public URI getResultCallbackUrl() {
        return resultCallbackUrl;
    }

    public void setResultCallbackUrl(URI result_callback_url) {
        this.resultCallbackUrl = result_callback_url;
    }

    public boolean isEngineeringBuild() {
        return engineeringBuild;
    }

    public void setEngineeringBuild(boolean engineeringBuild) {
        this.engineeringBuild = engineeringBuild;
    }

}
