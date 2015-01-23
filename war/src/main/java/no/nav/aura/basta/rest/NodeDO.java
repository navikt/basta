package no.nav.aura.basta.rest;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.basta.persistence.Node;
import no.nav.aura.basta.domain.input.vm.NodeStatus;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm.MiddleWareType;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeDO extends ModelEntityDO {

    private Set<OrderDO> orders;
    private URL adminUrl;
    private MiddleWareType middleWareType;
    private int cpuCount;
    private String datasenter;
    private String hostname;
    private int memoryMb;
    private String vapp;
    private OrderDO order;
    private URL fasitUrl;
    private URL fasitLookupUrl;
    private OrderDO decommissionOrder;
    private NodeStatus nodeStatus;

    @SuppressWarnings("unused")
    private NodeDO() {
    }

    public NodeDO(Node node, UriInfo uriInfo, boolean withOrders) {
        super(node);
        this.adminUrl = node.getAdminUrl();
        this.middleWareType = node.getMiddleWareType();
        this.cpuCount = node.getCpuCount();
        this.datasenter = node.getDatasenter();
        this.hostname = node.getHostname();
        this.memoryMb = node.getMemoryMb();
        this.vapp = node.getVapp();
        this.fasitUrl = node.getFasitUrl();
        this.fasitLookupUrl = getFasitLookupURL(fasitUrl, hostname);
        this.nodeStatus = node.getNodeStatus();
        if (withOrders) {
            this.orders = node.getOrders() == null ? null : orderDOsFromOrders(node.getOrders(), uriInfo);
            this.order = node.getOrder() == null ? null : new OrderDO(node.getOrder(), uriInfo);
            this.decommissionOrder = node.getDecommissionOrder() == null ? null : new OrderDO(node.getDecommissionOrder(), uriInfo);
        }

    }

    private URL getFasitLookupURL(URL fasitUrl, String hostname) {
        if (fasitUrl != null && !fasitUrl.getPath().contains("resources")) {
            try {
                return UriBuilder.fromUri(fasitUrl.toURI())
                        .replacePath("lookup")
                        .queryParam("type", "node")
                        .queryParam("name", hostname)
                        .build()
                        .toURL();
            } catch (URISyntaxException | MalformedURLException e) {
                throw new IllegalArgumentException("Illegal URL?", e);
            }
        }
        return fasitUrl;
    }

    private Set<OrderDO> orderDOsFromOrders(Set<Order> orders, UriInfo uriInfo) {
        Set<OrderDO> set = new HashSet<>();
        for (Order order : orders) {
            set.add(new OrderDO(order, uriInfo));
        }
        return set;

    }

    public String getVapp() {
        return vapp;
    }

    public void setVapp(String vapp) {
        this.vapp = vapp;
    }

    public int getMemoryMb() {
        return memoryMb;
    }

    public void setMemoryMb(int memoryMb) {
        this.memoryMb = memoryMb;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getDatasenter() {
        return datasenter;
    }

    public void setDatasenter(String datasenter) {
        this.datasenter = datasenter;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public MiddleWareType getMiddleWareType() {
        return middleWareType;
    }

    public void setMiddleWareType(MiddleWareType middleWareType) {
        this.middleWareType = middleWareType;
    }

    public URL getAdminUrl() {
        return adminUrl;
    }

    public void setAdminUrl(URL adminUrl) {
        this.adminUrl = adminUrl;
    }

    public OrderDO getOrder() {
        return order;
    }

    public void setOrder(OrderDO order) {
        this.order = order;
    }

    public OrderDO getDecommissionOrder() {
        return decommissionOrder;
    }

    public void setDecommissionOrder(OrderDO decommissionOrder) {
        this.decommissionOrder = decommissionOrder;
    }

    public URL getFasitUrl() {
        return fasitUrl;
    }

    public void setFasitUrl(URL fasitUrl) {
        this.fasitUrl = fasitUrl;
    }

    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public Set<OrderDO> getOrders() {
        return orders;
    }

    public void setOrders(Set<OrderDO> orders) {
        this.orders = orders;
    }

    public URL getFasitLookupUrl() {
        return fasitLookupUrl;
    }

    public void setFasitLookupUrl(URL fasitLookupUrl) {
        this.fasitLookupUrl = fasitLookupUrl;
    }
}
