package no.nav.aura.vmware.orchestrator.request;

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
public class Vm {

    public enum OSType {
        rel60
    };

    public enum MiddleWareType {
        wa, jp, ap
    }

    private OSType guestOs;
    private MiddleWareType type;
    private boolean dmz;
    private int cpuCount;
    private int diskSize;// i Gig = 20</size_disk> <!-- info: Size of main disk in GiB -->
    private int memorySize;// >16</size_memory>

    @XmlElementWrapper(name = "extradisks")
    @XmlElement(name = "disk")
    private List<Disk> disks = new ArrayList<>();

    @XmlElementWrapper(name = "customfacts")
    @XmlElement(name = "fact")
    private List<Fact> customFacts = new ArrayList<>();

    Vm() {
    }

    public Vm(OSType guestOs, MiddleWareType type, int cpucount, int size_disk, int size_memory, Disk... disks) {
        super();
        this.guestOs = guestOs;
        this.type = type;
        this.cpuCount = cpucount;
        this.diskSize = size_disk;
        this.memorySize = size_memory;
        this.disks = Lists.newArrayList(disks);
    }

    public OSType getGuestOs() {
        return guestOs;
    }

    public void setGuestOs(OSType guestOs) {
        this.guestOs = guestOs;
    }

    public MiddleWareType getType() {
        return type;
    }

    public void setType(MiddleWareType type) {
        this.type = type;
    }

    public boolean isDmz() {
        return dmz;
    }

    public void setDmz(boolean dmz) {
        this.dmz = dmz;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpucount) {
        this.cpuCount = cpucount;
    }

    public int getDiskSize() {
        return diskSize;
    }

    public void setDiskSize(int size_disk) {
        this.diskSize = size_disk;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(int size_memory) {
        this.memorySize = size_memory;
    }

    public List<Disk> getDisks() {
        return disks;
    }

    public void setDisks(List<Disk> disks) {
        this.disks = disks;
    }

    public List<Fact> getCustomFacts() {
        return customFacts;
    }

    public void setCustomFacts(List<Fact> facts) {
        this.customFacts = facts;
    }

}