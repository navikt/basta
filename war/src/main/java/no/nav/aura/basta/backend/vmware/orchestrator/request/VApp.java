package no.nav.aura.basta.backend.vmware.orchestrator.request;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.Lists;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class VApp {

    public enum Site {
        so8, u89
    }

    private Site site;
    private String description;

    @XmlElementWrapper(name = "vms")
    @XmlElement(name = "vm", required = true)
    private List<Vm> vms;

    VApp() {
    }

    public VApp(Site site, String description, Vm... vms) {
        this.site = site;
        this.description = description;
        this.vms = Lists.newArrayList(vms);
    }

    public VApp(Site site, Vm vm) {
        this(site, "", vm);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public List<Vm> getVms() {
        return vms;
    }

    public void addVm(Vm vm) {
        if (this.vms == null) {
            this.vms = new ArrayList<Vm>();
        }
        vms.add(vm);
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

}
