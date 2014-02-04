package no.nav.aura.basta.rest;

import java.net.URI;
import java.net.URL;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.basta.persistence.Node;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeDO extends ModelEntityDO {

    private URL adminUrl;
    private MiddleWareType middleWareType;
    private int cpuCount;
    private String datasenter;
    private String hostname;
    private int memoryMb;
    private String vapp;
    private Long id;
    private OrderDO order;
    private URI fasitUrl;
    private OrderDO decommissionOrder;

    @SuppressWarnings("unused")
    private NodeDO() {
    }

    public NodeDO(Node node, UriInfo uriInfo) {
        super(node);
        this.id = node.getId();
        this.adminUrl = node.getAdminUrl();
        this.middleWareType = node.getMiddleWareType();
        this.cpuCount = node.getCpuCount();
        this.datasenter = node.getDatasenter();
        this.hostname = node.getHostname();
        this.memoryMb = node.getMemoryMb();
        this.vapp = node.getVapp();
        this.fasitUrl = node.getFasitUrl();
        this.order = new OrderDO(node.getOrder(), uriInfo);
        this.decommissionOrder = new OrderDO(node.getDecommissionOrder(), uriInfo);
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderDO getOrder() {
        return order;
    }

    public void setOrder(OrderDO order) {
        this.order = order;
    }

    public URI getFasitUrl() {
        return fasitUrl;
    }

    public void setFasitUrl(URI fasitUrl) {
        this.fasitUrl = fasitUrl;
    }

    public OrderDO getDecommissionOrder() {
        return decommissionOrder;
    }

    public void setDecommissionOrder(OrderDO decommissionOrder) {
        this.decommissionOrder = decommissionOrder;
    }

}
