package no.nav.aura.basta.backend.vmware.orchestrator.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class OperationResponseVm {

    public enum ResultType {
        on, off, error
    }

    public OperationResponseVm() {
    }

    public OperationResponseVm(String hostname, ResultType result) {
        this.hostName = hostname;
        this.result = result;
    }

    private String hostName;
    private ResultType result;

    public String getHostname() {
        return hostName;
    }

    public ResultType getResult() {
        return result;
    }
}
