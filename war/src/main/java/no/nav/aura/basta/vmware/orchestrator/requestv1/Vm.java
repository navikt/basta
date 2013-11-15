package no.nav.aura.basta.vmware.orchestrator.requestv1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class Vm {
    private String guestOs;
    private String size;
    private String type;
    private boolean dmz;

    private List<Disk> disks;

    public Vm() {
    }

    private Vm(String guestOs, String size, String type, boolean dmz) {
        this.guestOs = guestOs;
        this.size = size;
        this.type = type;
        this.dmz = dmz;
    }

    public Vm(String guestOs, String size, String type, List<Disk> disks) {
        this(guestOs, size, type, false);
        this.disks = disks;
    }

    public Vm(String guestOs, String size, String type, Disk disk) {
        this(guestOs, size, type, false);
        this.addDisk(disk);
    }

    public Vm(String guestOs, String size, String type, Disk disk, boolean dmz) {
        this(guestOs, size, type, false);
        this.dmz = dmz;
        this.addDisk(disk);
    }

    @XmlElement
    public String getGuestOs() {
        return guestOs;
    }

    @XmlElement
    public String getSize() {
        return size;
    }

    @XmlElement
    public String getType() {
        return type;
    }

    @XmlElement(name = "disk", required = true)
    public List<Disk> getDisks() {
        return disks;
    }

    public void addDisk(Disk disk) {
        if (this.disks == null) {
            this.disks = new ArrayList<Disk>();
        }
        disks.add(disk);
    }

    public boolean isDmz() {
        return dmz;
    }

    @XmlElement
    public void setDmz(boolean dmz) {
        this.dmz = dmz;
    }
}