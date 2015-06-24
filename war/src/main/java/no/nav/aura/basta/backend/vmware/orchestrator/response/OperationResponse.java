package no.nav.aura.basta.backend.vmware.orchestrator.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import no.nav.aura.basta.backend.vmware.orchestrator.response.OperationResponseVm.ResultType;

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


    public static OperationResponse createOperation(ResultType type, List<String> hostnames) {
        OperationResponse response = new OperationResponse();
        List<OperationResponseVm> vmList = new ArrayList<>();
        for (String hostname : hostnames) {
            vmList.add(new OperationResponseVm(hostname, type));
        }
        response.setVms(vmList);
        return response;
    }

}
