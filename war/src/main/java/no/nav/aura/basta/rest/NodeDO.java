package no.nav.aura.basta.rest;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

;
import no.nav.aura.basta.domain.input.vm.NodeStatus;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm.MiddleWareType;

import com.sun.xml.txw2.annotation.XmlElement;
import no.nav.aura.basta.domain.result.vm.VMNode;

import static java.lang.System.getProperty;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeDO {

    private String hostname;
    private URL fasitLookupUrl;
    private NodeStatus nodeStatus;

    private List<OrderDO> history;

    @SuppressWarnings("unused")
    private NodeDO() {
    }

    public NodeDO(String hostname, NodeStatus nodeStatus, UriInfo uriInfo, boolean withOrders) {

        this.hostname = hostname;
        this.fasitLookupUrl = getFasitLookupURL(hostname);
        this.nodeStatus = nodeStatus;


    }

    public NodeDO(VMNode vmNode, UriInfo uriInfo) {
        this.hostname = vmNode.getHostname();
        this.fasitLookupUrl = vmNode.getFasitUrl();
        this.nodeStatus = vmNode.getStatus();
    }

    private URL getFasitLookupURL (String hostname) {
            try {
                return UriBuilder.fromUri(getProperty("fasit.rest.api.url"))
                        .replacePath("lookup")
                        .queryParam("type", "node")
                        .queryParam("name", hostname)
                        .build()
                        .toURL();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Illegal URL?", e);
            }
    }


    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public URL getFasitLookupUrl() {
        return fasitLookupUrl;
    }

    public void setFasitLookupUrl(URL fasitLookupUrl) {
        this.fasitLookupUrl = fasitLookupUrl;
    }

    public List<OrderDO> getHistory() {
        return history;
    }

    public void setHistory(List<OrderDO> history) {
        this.history = history;
    }
}
