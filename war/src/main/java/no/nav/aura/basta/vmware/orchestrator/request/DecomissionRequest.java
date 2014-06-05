package no.nav.aura.basta.vmware.orchestrator.request;

import java.net.URI;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "orchestratorRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class DecomissionRequest implements OrchestatorRequest {
    public DecomissionRequest() {
    }

    private URI statusCallbackUrl;
    private URI resultCallbackUrl;

    @XmlElement(name = "removeVM", required = true)
    private List<String> vmsToRemove;

    public DecomissionRequest(List<String> vms) {
        this.vmsToRemove = vms;
    }

    public List<String> getVmsToRemove() {
        return vmsToRemove;
    }

    public void setVmsToRemove(List<String> vmsToRemove) {
        this.vmsToRemove = vmsToRemove;
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
}
