package no.nav.aura.basta.vmware.orchestrator.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "orchestratorResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class OrchestratorResponse {

    private boolean deploymentSuccess;
    private String err;

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }

    public boolean isDeploymentSuccess() {
        return deploymentSuccess;
    }

    public void setDeploymentSuccess(boolean deploymentSuccess) {
        this.deploymentSuccess = deploymentSuccess;
    }

}
