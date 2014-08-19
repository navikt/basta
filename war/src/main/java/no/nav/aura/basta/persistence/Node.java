package no.nav.aura.basta.persistence;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;
import org.apache.commons.lang.builder.ToStringBuilder;

@Entity
@Table
@SequenceGenerator(name="hibernate_sequence", sequenceName="hibernate_sequence")
public class Node extends ModelEntity {


    private String hostname;
    private URL adminUrl;
    private int cpuCount;
    private int memoryMb;
    private String datasenter;
    @Enumerated(EnumType.STRING)
    private MiddleWareType middleWareType;
    private String vapp;
    private URL fasitUrl;

    @Enumerated(EnumType.STRING)
    private NodeType nodeType;

    @Enumerated(EnumType.STRING)
    private NodeStatus nodeStatus;

    @ManyToMany(mappedBy = "nodes")
    private Set<Order> orders = new HashSet<>();

    public Node() {
    }

    public Node(Order order, NodeType nodeType, String hostname, URL adminUrl, int cpuCount, int memoryMb, String datasenter, MiddleWareType middleWareType, String vapp) {
        this.nodeType = nodeType;
        this.orders.add( order);
        this.hostname = hostname;
        this.adminUrl = adminUrl;
        this.cpuCount = cpuCount;
        this.memoryMb = memoryMb;
        this.datasenter = datasenter;
        this.middleWareType = middleWareType;
        this.vapp = vapp;
        this.nodeStatus=NodeStatus.ACTIVE;
    }

    public Order getOrder() {
        return FluentIterable.from(orders).filter(new Predicate<Order>() {
            @Override
            public boolean apply(Order order) {
                return order.getOrderType().equals(OrderType.PROVISION);
            }
        }).first().orNull();
    }

    public void setOrder(Order order) {
        this.orders.add(order);
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public URL getAdminUrl() {
        return adminUrl;
    }

    public void setAdminUrl(URL adminUrl) {
        this.adminUrl = adminUrl;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public int getMemoryMb() {
        return memoryMb;
    }

    public void setMemoryMb(int memoryMb) {
        this.memoryMb = memoryMb;
    }

    public String getDatasenter() {
        return datasenter;
    }

    public void setDatasenter(String datasenter) {
        this.datasenter = datasenter;
    }

    public MiddleWareType getMiddleWareType() {
        return middleWareType;
    }

    public void setMiddleWareType(MiddleWareType middleWareType) {
        this.middleWareType = middleWareType;
    }

    public String getVapp() {
        return vapp;
    }

    public void setVapp(String vapp) {
        this.vapp = vapp;
    }

    public URL getFasitUrl() {
        return fasitUrl;
    }

    public void setFasitUrl(URL fasitUrl) {
        this.fasitUrl = fasitUrl;
    }

    public Order getDecommissionOrder() {
        return FluentIterable.from(orders).filter(new Predicate<Order>() {
            @Override
            public boolean apply(Order order) {
                return order.getOrderType().equals(OrderType.DECOMMISSION);
            }
        }).first().orNull();
    }

    public void setDecommissionOrder(Order decommissionOrder) {
        this.orders.add(decommissionOrder);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }
}
