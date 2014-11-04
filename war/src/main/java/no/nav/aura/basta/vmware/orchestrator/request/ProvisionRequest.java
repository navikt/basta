package no.nav.aura.basta.vmware.orchestrator.request;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProvisionRequest implements OrchestatorRequest, Cloneable {

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

        public static OrchestratorEnvClass fromString(String name){
                for (OrchestratorEnvClass orchestratorEnvClass : values()) {
                    if (orchestratorEnvClass.getName().equals(name)){
                        return orchestratorEnvClass;
                    }
                }
            throw new IllegalArgumentException("Unknown representation: " + name );
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
    private String applicationMappingName;
    private String application;  // TODO Remove this when Orchestrator supports applicationGroups. This is only here to preserve backwards compatability. When Roger D. is back from holliday
    @XmlElementWrapper(name = "applications")
    @XmlElement(name = "application")
    private List<String> applications;
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

    public String getApplicationMappingName() {
        return applicationMappingName;
    }

    public void setApplicationMappingName(String applicationMappingName) {
        this.applicationMappingName = applicationMappingName;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public List<String> getApplications() {
        return applications;
    }

   public void setApplications(List<String> applications) {
        this.applications = applications;
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


    /**
     * @return same as input, but now censored
     */
    public ProvisionRequest censore()  {

            //ProvisionRequest provisionRequest = (ProvisionRequest)this.clone();
            for (VApp vapp : Optional.fromNullable(this.getvApps()).or(Lists.<VApp> newArrayList())) {
                for (Vm vm : Optional.fromNullable(vapp.getVms()).or(Lists.<Vm> newArrayList())) {
                    for (Fact fact : Optional.fromNullable(vm.getCustomFacts()).or(Lists.<Fact> newArrayList())) {
                        if (FactType.valueOf(fact.getName()).isMask()) {
                            fact.setValue("********");
                        }
                    }
                }
            }
            return this;
    }
}
