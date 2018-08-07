package no.nav.aura.basta.backend.vmware.orchestrator.request;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorEnvironmentClass;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.security.User;

@XmlRootElement(name = "provisionRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProvisionRequest implements OrchestatorRequest {

	private URI statusCallbackUrl;
	private URI resultCallbackUrl;
	private String environmentId;
    private String orderedBy;
	private OrchestratorEnvironmentClass environmentClass;
	@XmlElement(name = "application")
	private List<String> applications;

	@XmlElementWrapper(name = "vms")
	@XmlElement(name = "vm", required = true)
	private List<Vm> vms = new ArrayList<>();

    protected ProvisionRequest() {
	}

    public ProvisionRequest(VMOrderInput input, URI resultCallbackUrl, URI statusCallbackUrl) {
        this(OrchestratorEnvironmentClass.convert(input), input, resultCallbackUrl, statusCallbackUrl);
    }

    public ProvisionRequest(OrchestratorEnvironmentClass environmentClass, VMOrderInput input, URI resultCallbackUrl, URI statusCallbackUrl) {
        this.environmentClass = environmentClass;
        this.resultCallbackUrl = resultCallbackUrl;
        this.statusCallbackUrl = statusCallbackUrl;
        this.setApplications(input.getApplicationMappingName());
        this.setEnvironmentId(input.getEnvironmentName());
        this.orderedBy = User.getCurrentUser().getName();
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

	public String getEnvironmentId() {
		return environmentId;
	}

	public void setEnvironmentId(String environmentId) {
		this.environmentId = environmentId;
	}

	public OrchestratorEnvironmentClass getEnvironmentClass() {
		return environmentClass;
	}

	public void setEnvironmentClass(OrchestratorEnvironmentClass environmentClass) {
		this.environmentClass = environmentClass;
	}

	public List<String> getApplications() {
		return applications;
	}

	public void setApplications(String... applications) {
		this.applications = Arrays.asList(applications);
	}

	public List<Vm> getVms() {
		return vms;
	}

	public void addVm(Vm vm) {
		this.vms.add(vm);
	}

    public String getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(String orderedBy) {
        this.orderedBy = orderedBy;
    }

}
