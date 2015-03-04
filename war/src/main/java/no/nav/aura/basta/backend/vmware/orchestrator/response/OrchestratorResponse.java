package no.nav.aura.basta.backend.vmware.orchestrator.response;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "orchestratorResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class OrchestratorResponse {

    private boolean deploymentSuccess;
    private String err;
    @XmlElement(name = "vm")
    private List<Vm> vms;
    private String finishTime;

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

    public List<Vm> getVms() {
        return vms;
    }

    public void setVms(List<Vm> vms) {
        this.vms = vms;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

}
