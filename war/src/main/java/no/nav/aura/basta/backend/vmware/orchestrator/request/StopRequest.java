package no.nav.aura.basta.backend.vmware.orchestrator.request;


import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.List;

@XmlRootElement(name = "orchestratorRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class StopRequest implements OrchestatorRequest {

    private URI stopCallbackUrl;
    private URI statusCallbackUrl;

    @XmlElement(name = "powerdown", required = true)
    private List<String> powerdown;

    public StopRequest() {
    }

    public StopRequest(String[] hostnames, URI stopCallbackUrl, URI bastaStatusUri) {

        if (hostnames == null || hostnames.length == 0) {
            throw new IllegalArgumentException("No hostnames");
        }
        this.stopCallbackUrl = stopCallbackUrl;
        this.statusCallbackUrl = bastaStatusUri;
        this.powerdown = OrchestratorUtil.stripFqdnFromHostnames(hostnames);
    }




    public URI getStatusCallbackUrl() {
        return statusCallbackUrl;
    }

    public void setStatusCallbackUrl(URI statusCallbackUrl) {
        this.statusCallbackUrl = statusCallbackUrl;
    }


    public URI getStopCallbackUrl() {
        return stopCallbackUrl;
    }

    public void setStopCallbackUrl(URI stopCallbackUrl) {
        this.stopCallbackUrl = stopCallbackUrl;
    }

    public List<String> getPowerdown() {
        return powerdown;
    }

    public void setPowerdown(List<String> powerdown) {
        this.powerdown = powerdown;
    }

	@Override
	public OrchestatorRequest censore() {
		return this;
	}
}
