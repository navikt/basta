package no.nav.aura.basta.backend.vmware.orchestrator.request;

import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorUtil;

import java.net.URI;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "orchestratorRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class DecomissionRequest implements OrchestatorRequest {
    private URI decommissionCallbackUrl;
    private URI statusCallbackUrl;


    public DecomissionRequest() {
    }



    @XmlElement(name = "removeVM", required = true)
    private List<String> vmsToRemove;

    public DecomissionRequest(String[] hostnames, URI decommissionUri, URI bastaStatusUri) {

        if (hostnames == null || hostnames.length == 0) {
            throw new IllegalArgumentException("No hostnames");
        }
        this.setDecommissionCallbackUrl(decommissionUri);
        this.setStatusCallbackUrl(bastaStatusUri);
        this.vmsToRemove = OrchestratorUtil.stripFqdnFromHostnames(hostnames);
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


    public void setDecommissionCallbackUrl(URI decommissionCallbackUrl) {
        this.decommissionCallbackUrl = decommissionCallbackUrl;
    }

    public URI getDecommissionCallbackUrl() {
        return decommissionCallbackUrl;
    }

}
