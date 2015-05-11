package no.nav.aura.basta.backend.vmware.orchestrator.v2;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;



@XmlRootElement(name = "ProvisionRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProvisionRequest2 implements OrchestatorRequest {

	public enum OrchestratorEnvironmentClass {
		utv, test, preprod, qa, prod
	}

	private URI statusCallbackUrl;
	private URI resultCallbackUrl;
	private String environmentId;
	private OrchestratorEnvironmentClass environmentClass;
	@XmlElement(name = "application")
	private List<String> applications;

	@XmlElementWrapper(name = "vms")
	@XmlElement(name = "vm", required = true)
	private List<Vm> vms = new ArrayList<>();

	public ProvisionRequest2() {
	}

	public ProvisionRequest2(OrchestratorEnvironmentClass environmentClass) {
		this.environmentClass = environmentClass;
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

}
