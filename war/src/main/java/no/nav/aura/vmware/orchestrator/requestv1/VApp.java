package no.nav.aura.vmware.orchestrator.requestv1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class VApp {

    private String site;
    private String description;

    private List<Vm> vms;

    public VApp() {
    }

    public VApp(String site, String description) {
        this.site = site;
        this.description = description;
    }

    public VApp(String site, Vm vm) {
        this(site, "");
        addVm(vm);
    }

    @XmlElement
    public String getSite() {
        return site;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement
    public String getDescription() {
        return description;
    }

    @XmlElementWrapper(name = "vms")
    @XmlElement(name = "vm", required = true)
    public List<Vm> getVms() {
        return vms;
    }

    public void addVm(Vm vm) {
        if (this.vms == null) {
            this.vms = new ArrayList<Vm>();
        }
        vms.add(vm);
    }

    public void setSite(String site) {
        this.site = site;
    }
}
