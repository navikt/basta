package no.nav.aura.basta.rest.vm.dataobjects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "vms")
@XmlAccessorType(XmlAccessType.FIELD)
public class OrchestratorNodeDOList {

    @XmlElement(name="vm")
    private List<OrchestratorNodeDO> vms;

    public OrchestratorNodeDOList() {
        this.vms = new ArrayList<>();
    }


    public List<OrchestratorNodeDO> getVms() {
        return vms;
    }

    public void setVms(List<OrchestratorNodeDO> vms) {
        this.vms = vms;
    }

    public void addVM(OrchestratorNodeDO vm) {
        this.vms.add(vm);
    }

}
