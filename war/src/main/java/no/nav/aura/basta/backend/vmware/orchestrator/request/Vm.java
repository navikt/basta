package no.nav.aura.basta.backend.vmware.orchestrator.request;

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
        rhel60
    };

    public enum MiddleWareType {
        wa, jb, ap
    }

    private OSType guestOs;
    private MiddleWareType type;
    private boolean dmz;
    private int cpuCount;
    private int memorySize;// >16</size_memory>

    @XmlElementWrapper(name = "extradisks")
    @XmlElement(name = "disk")
    private List<Disk> disks;

    @XmlElementWrapper(name = "customfacts")
    @XmlElement(name = "fact")
    private List<Fact> customFacts;
    private String description;

    Vm() {
    }

    public Vm(OSType guestOs, MiddleWareType type, int cpucount, int memorySize, Disk... disks) {
        super();
        this.guestOs = guestOs;
        this.type = type;
        this.cpuCount = cpucount;
        this.memorySize = memorySize;
        this.disks = disks.length == 0 ? null : Lists.newArrayList(disks);
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

    public int getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(int memorySize) {
        this.memorySize = memorySize;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}