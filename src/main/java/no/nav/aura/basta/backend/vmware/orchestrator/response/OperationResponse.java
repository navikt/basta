package no.nav.aura.basta.backend.vmware.orchestrator.response;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "operationResponse")
public class OperationResponse {
    

    @XmlElement(name = "vm")
    private List<OperationResponseVm> vms;

    public List<OperationResponseVm> getVms() {
        return vms;
    }

    public void setVms(List<OperationResponseVm> vms) {
        this.vms = vms;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("vms", vms).build();
    }

}
