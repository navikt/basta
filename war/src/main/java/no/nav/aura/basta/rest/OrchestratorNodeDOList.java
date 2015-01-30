package no.nav.aura.basta.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "vms")
@XmlAccessorType(XmlAccessType.FIELD)
public class OrchestratorNodeDOList {

    private List<OrchestratorNodeDO> vms;

    public OrchestratorNodeDOList(){
        this.vms = new ArrayList<>();
    }


    public OrchestratorNodeDOList(List<OrchestratorNodeDO> vms) {
        this.vms = vms;
    }

    public List<OrchestratorNodeDO> getVms() {
        return vms;
    }

    public void setVms(List<OrchestratorNodeDO> vms) {
        this.vms = vms;
    }

    public void addVM(OrchestratorNodeDO vm){
        this.vms.add(vm);
    }

}
