package no.nav.aura.vmware.orchestrator.request;

import javax.xml.bind.annotation.XmlElement;

public class Disk {
    private String size;

    public Disk() {
    }

    public Disk(String size) {
        this.size = size;
    }

    @XmlElement
    public String getSize() {
        return size;
    }
}