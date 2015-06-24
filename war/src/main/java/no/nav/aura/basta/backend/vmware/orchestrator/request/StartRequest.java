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
public class StartRequest implements OrchestatorRequest {


    private URI resultCallbackUrl;
    private URI statusCallbackUrl;

    @XmlElement(name = "poweron", required = true)
    private List<String> poweron;

    public StartRequest() {
    }

    public StartRequest(String[] hostnames, URI startCallbackUrl, URI statusCallBackUrl) {

        if (hostnames == null || hostnames.length == 0) {
            throw new IllegalArgumentException("No hostnames");
        }
        this.resultCallbackUrl = startCallbackUrl;
        this.statusCallbackUrl = statusCallBackUrl;
        this.poweron = OrchestratorUtil.stripFqdnFromHostnames(hostnames);
    }

    public URI getStatusCallbackUrl() {
        return statusCallbackUrl;
    }

    public void setStatusCallbackUrl(URI statusCallbackUrl) {
        this.statusCallbackUrl = statusCallbackUrl;
    }

    public URI getStartCallbackUrl() {
        return resultCallbackUrl;
    }

    public void setStartCallbackUrl(URI startCallbackUrl) {
        this.resultCallbackUrl = startCallbackUrl;
    }

    public List<String> getPoweron() {
        return poweron;
    }

    public void setPoweron(List<String> poweron) {
        this.poweron = poweron;
    }

}
