package no.nav.aura.basta.vmware.orchestrator.request;


import no.nav.aura.basta.vmware.orchestrator.OrchestratorUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

@XmlRootElement(name = "orchestratorRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class StartRequest implements OrchestatorRequest {

    @XmlElement(name = "poweron", required = true)
    private String poweron;
    private URI stopCallbackUrl;
    private URI statusCallbackUrl;

    public StartRequest(String hostname, URI stopCallbackUrl, URI statusCallBackUrl) {

        if (hostname == null || hostname.isEmpty()) {
            throw new IllegalArgumentException("No hostname");
        }
        this.stopCallbackUrl = stopCallbackUrl;
        this.statusCallbackUrl = statusCallBackUrl;
        this.poweron = OrchestratorUtil.stripFqdnFromHostnames(new String[]{hostname}).get(0);
    }

    public String getPoweron() {
        return poweron;
    }

    public void setPoweron(String poweron) {
        this.poweron = poweron;
    }

    public URI getStopCallbackUrl() {
        return stopCallbackUrl;
    }

    public void setStopCallbackUrl(URI stopCallbackUrl) {
        this.stopCallbackUrl = stopCallbackUrl;
    }

    public URI getStatusCallbackUrl() {
        return statusCallbackUrl;
    }

    public void setStatusCallbackUrl(URI statusCallbackUrl) {
        this.statusCallbackUrl = statusCallbackUrl;
    }
}
