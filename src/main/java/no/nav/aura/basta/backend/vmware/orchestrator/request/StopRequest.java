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

    private URI resultCallbackUrl;
    private URI statusCallbackUrl;

    @XmlElement(name = "poweroff", required = true)
    private List<String> poweroff;

    public StopRequest() {
    }

    public StopRequest(String[] hostnames, URI stopCallbackUrl, URI bastaStatusUri) {

        if (hostnames == null || hostnames.length == 0) {
            throw new IllegalArgumentException("No hostnames");
        }
        this.resultCallbackUrl = stopCallbackUrl;
        this.statusCallbackUrl = bastaStatusUri;
        this.poweroff = OrchestratorUtil.stripFqdnFromHostnames(hostnames);
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

    public void setResultCallbackUrl(URI stopCallbackUrl) {
        this.resultCallbackUrl = stopCallbackUrl;
    }

    public List<String> getPoweroff() {
        return poweroff;
    }

    public void setPoweroff(List<String> powerdown) {
        this.poweroff = powerdown;
    }

}
