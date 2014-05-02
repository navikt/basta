package no.nav.aura.basta.persistence;

import java.net.URL;

import javax.persistence.*;

import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;
import org.apache.commons.lang.builder.ToStringBuilder;

@Entity
@Table
@SequenceGenerator(name="hibernate_sequence", sequenceName="hibernate_sequence")
public class Node extends ModelEntity {

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "orderId")
    private Order order;
    private String hostname;
    private URL adminUrl;
    private int cpuCount;
    private int memoryMb;
    private String datasenter;
    @Enumerated(EnumType.STRING)
    private MiddleWareType middleWareType;
    private String vapp;
    private URL fasitUrl;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "decommissionOrderId")
    private Order decommissionOrder;

    public Node() {
    }

    public Node(Order order, String hostname, URL adminUrl, int cpuCount, int memoryMb, String datasenter, MiddleWareType middleWareType, String vapp) {
        this.order = order;
        this.hostname = hostname;
        this.adminUrl = adminUrl;
        this.cpuCount = cpuCount;
        this.memoryMb = memoryMb;
        this.datasenter = datasenter;
        this.middleWareType = middleWareType;
        this.vapp = vapp;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
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
        return decommissionOrder;
    }

    public void setDecommissionOrder(Order decommissionOrder) {
        this.decommissionOrder = decommissionOrder;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
