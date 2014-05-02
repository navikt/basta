package no.nav.aura.basta.vmware.orchestrator.request;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Optional;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProvisionRequest implements OrchestatorRequest {

    public enum Zone {
        sbs, fss
    };

    public enum OrchestratorEnvClass {
        utv(Optional.of("ikt\\utv")), test(Optional.of("ikt\\test")), preprod(Optional.of("ikt\\preprod")), qa(Optional.of("ikt\\qa")), prod(Optional.<String> absent());

        private final Optional<String> nameInU;

        private OrchestratorEnvClass(Optional<String> nameInU) {
            this.nameInU = nameInU;
        }

        public String getName() {
            if ("u".equals(System.getProperty("environment.class"))) {
                if (nameInU.isPresent()) {
                    return nameInU.get();
                } else {
                    throw new RuntimeException("Orchestrator does not have a representation for " + this + " in u");
                }
            }
            return name();
        }
    };

    public enum Role {
        was, /* Seriously! Should be BPM */wps, div
    }

    private String environmentId;
    private Zone zone;
    private String owner; // remove
    private String orderedBy;
    private String environmentClass;
    private String application;
    private boolean changeDeployerPassword = false; // Optional Default False
    private Role role;// was|bpm|div</role> <!-- orchName: vAppRole, info: Part of the logic to choose the right org-vdc -->

    private URI statusCallbackUrl;
    private URI resultCallbackUrl;
    private Boolean engineeringBuild;

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

    public String getEnvironmentClass() {
        return environmentClass;
    }

    public void setEnvironmentClass(String environmentClass) {
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

    public boolean getChangeDeployerPassword() {
        return changeDeployerPassword;
    }

    public void setChangeDeployerPassword(boolean changeDeployerPassword) {
        this.changeDeployerPassword = changeDeployerPassword;
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

    public void setStatusCallbackUrl(URI statusCallbackUrl) {
        this.statusCallbackUrl = statusCallbackUrl;
    }

    public URI getResultCallbackUrl() {
        return resultCallbackUrl;
    }

    public void setResultCallbackUrl(URI resultCallbackUrl) {
        this.resultCallbackUrl = resultCallbackUrl;
    }

    public Boolean isEngineeringBuild() {
        return engineeringBuild;
    }

    public void setEngineeringBuild(Boolean engineeringBuild) {
        this.engineeringBuild = engineeringBuild;
    }

}
