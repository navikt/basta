package no.nav.aura.basta.backend.vmware.orchestrator.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
