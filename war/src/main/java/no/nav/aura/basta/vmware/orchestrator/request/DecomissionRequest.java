package no.nav.aura.basta.vmware.orchestrator.request;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import no.nav.aura.basta.persistence.Hostnames;
import no.nav.aura.basta.util.SerializableFunction;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "orchestratorRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class DecomissionRequest implements OrchestatorRequest {
    private URI decommissionCallbackUrl;
    private URI statusCallbackUrl;


    public DecomissionRequest() {
    }



    @XmlElement(name = "removeVM", required = true)
    private List<String> vmsToRemove;

    public DecomissionRequest(String[] hostnames, URI decommissionUri, URI bastaStatusUri) {

        if (hostnames == null || hostnames.length == 0) {
            throw new IllegalArgumentException("No hostnames");
        }
        this.setDecommissionCallbackUrl(decommissionUri);
        this.setStatusCallbackUrl(bastaStatusUri);
        this.vmsToRemove = stripFqdnFromHostnames(hostnames);
    }

    private ImmutableList<String> stripFqdnFromHostnames(String[] hostnames) {
        return FluentIterable.from(Arrays.asList(hostnames))
                       .transform(new SerializableFunction<String, String>() {
                           public String process(String input) {
                               int idx = input.indexOf('.');
                               return input.substring(0, idx != -1 ? idx : input.length());
                           }
                       }).toList();
    }


    public List<String> getVmsToRemove() {
        return vmsToRemove;
    }

    public void setVmsToRemove(List<String> vmsToRemove) {
        this.vmsToRemove = vmsToRemove;
    }

    public URI getStatusCallbackUrl() {
        return statusCallbackUrl;
    }

    public void setStatusCallbackUrl(URI statusCallbackUrl) {
        this.statusCallbackUrl = statusCallbackUrl;
    }


    public void setDecommissionCallbackUrl(URI decommissionCallbackUrl) {
        this.decommissionCallbackUrl = decommissionCallbackUrl;
    }

    public URI getDecommissionCallbackUrl() {
        return decommissionCallbackUrl;
    }
}
