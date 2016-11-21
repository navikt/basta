package no.nav.aura.basta.rest.vm.dataobjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
