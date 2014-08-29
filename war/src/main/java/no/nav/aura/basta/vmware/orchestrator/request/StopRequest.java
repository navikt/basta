package no.nav.aura.basta.vmware.orchestrator.request;


import no.nav.aura.basta.vmware.orchestrator.OrchestratorUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

@XmlRootElement(name = "orchestratorRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class StopRequest implements OrchestatorRequest {

    @XmlElement(name = "powerdown", required = true)
    private String powerdown;
    private URI stopCallbackUrl;
    private URI statusCallbackUrl;

    public StopRequest(String hostname, URI stopCallbackUrl, URI bastaStatusUri) {

        if (hostname == null || hostname.isEmpty()) {
            throw new IllegalArgumentException("No hostname");
        }
        this.stopCallbackUrl = stopCallbackUrl;
        this.statusCallbackUrl = bastaStatusUri;
        this.powerdown = OrchestratorUtil.stripFqdnFromHostnames(new String[]{hostname}).get(0);
    }


    public String getPowerdown() {
        return powerdown;
    }

    public void setPowerdown(String powerdown) {
        this.powerdown = powerdown;
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
}
