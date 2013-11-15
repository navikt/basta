package no.nav.aura.basta.vmware.orchestrator.requestv1;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import no.nav.aura.basta.vmware.orchestrator.request.OrchestatorRequest;

@XmlRootElement(name = "orchestratorRequest")
public class DecomissionRequest implements OrchestatorRequest {
    public DecomissionRequest() {
    }

    @XmlElement(name = "removeVM", required = true)
    private List<String> vmsToRemove;

    public DecomissionRequest(List<String> vms) {
        this.vmsToRemove = vms;
    }
}
