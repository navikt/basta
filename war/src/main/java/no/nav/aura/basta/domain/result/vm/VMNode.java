package no.nav.aura.basta.domain.result.vm;

import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.NodeStatus;
import no.nav.aura.basta.domain.input.vm.NodeType;

import javax.ws.rs.core.UriBuilder;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Set;

import static java.lang.System.getProperty;


public class VMNode implements Comparable<VMNode> {

    private String hostname;
    private URL fasitUrl;
    private NodeStatus status;
    private final NodeType nodeType;

    private Set<Order> history;



    public VMNode(String hostname, NodeStatus status, NodeType nodeType) {
        this.hostname = hostname;
        this.status = status;
        this.nodeType = nodeType;
        this.fasitUrl = getFasitLookupURL();
    }

    private URL getFasitLookupURL () {
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

    public String getHostname(){
        return hostname;
    }

    public void setHostname(String hostname){
        this.hostname =hostname;
    }

    public URL getFasitUrl() {
        return fasitUrl;
    }

    public void setFasitUrl(URL fasitUrl) {
        this.fasitUrl = fasitUrl;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    @Override
    public int compareTo(VMNode o) {
        return hostname.compareTo(o.getHostname());
    }

    public Set<Order> getHistory() {
        return history;
    }

    public void setHistory(Set<Order> history) {
        this.history = history;
    }

    public NodeType getNodeType() {
        return nodeType;
    }
}
