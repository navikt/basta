package no.nav.aura.basta.backend.vmware.orchestrator.v2;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddleWareType;
import no.nav.aura.basta.backend.vmware.orchestrator.OSType;
import no.nav.aura.basta.domain.input.Zone;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Vm {

    private Zone zone;
    private Boolean changeDeployerPassword;
    private Classification classification;
    @XmlElement(name = "os")
    private OSType guestOs;
    private MiddleWareType type;
    private int cpuCount;
    private int memorySize;
    private Integer extraDisk;

    @XmlElementWrapper(name = "annotations")
    @XmlElement(name = "annotation")
    private List<KeyValue> annotations = new ArrayList<>();

    @XmlElementWrapper(name = "customfacts")
    @XmlElement(name = "fact")
    private List<KeyValue> customFacts = new ArrayList<>();

    Vm() {
    }

    public Vm(Zone zone, OSType guestOs, MiddleWareType type, Classification classification, int cpucount, int memorySize) {
        this.zone = zone;
        this.guestOs = guestOs;
        this.type = type;
        this.classification = classification;
        this.cpuCount = cpucount;
        this.memorySize = memorySize;
    }

    public void setDescription(String description) {
        if (description != null) {
            addAnnotation("Notes", description);
        }
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

    public List<KeyValue> getCustomFacts() {
        return customFacts;
    }

    public void setCustomFacts(List<KeyValue> facts) {
        this.customFacts = facts;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public boolean isChangeDeployerPassword() {
        return changeDeployerPassword;
    }

    public void setChangeDeployerPassword(boolean changeDeployerPassword) {
        this.changeDeployerPassword = changeDeployerPassword;
    }

    public Classification getClassification() {
        return classification;
    }

    public void setClassification(Classification classification) {
        this.classification = classification;
    }

    public int getExtraDisk() {
        return extraDisk;
    }

    public void setExtraDisk(int extraDisk) {
        this.extraDisk = extraDisk;
    }

    public List<KeyValue> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<KeyValue> annotations) {
        this.annotations = annotations;
    }

    public void addAnnotation(String name, String value) {
        this.annotations.add(new KeyValue(name, value));
    }

    public void addPuppetFact(String name, String value) {
        this.customFacts.add(new KeyValue(name, value));
    }

    public void setExtraDiskAsGig(Integer extraDiskasGig) {
        if (extraDiskasGig != null) {
            this.extraDisk = extraDiskasGig;
        }

    }

}