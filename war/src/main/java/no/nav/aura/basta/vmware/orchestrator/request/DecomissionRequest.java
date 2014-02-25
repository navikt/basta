package no.nav.aura.basta.vmware.orchestrator.request;

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

}
