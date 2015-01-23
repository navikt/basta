package no.nav.aura.basta.backend.vmware.orchestrator.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Disk {
    private int size;

    protected Disk() {
    }

    public Disk(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}