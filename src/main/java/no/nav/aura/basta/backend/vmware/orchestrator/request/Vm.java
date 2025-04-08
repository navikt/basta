package no.nav.aura.basta.backend.vmware.orchestrator.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlType;

import no.nav.aura.basta.backend.fasit.deprecated.payload.Zone;
import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddlewareType;
import no.nav.aura.basta.backend.vmware.orchestrator.OSType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Vm {

    private Zone zone;
    private Boolean changeDeployerPassword;
    private Classification classification;
    @XmlElement(name = "os")
    private OSType guestOs;
    private MiddlewareType type;
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

    public Vm(VMOrderInput input) {
        this.zone = input.getZone();
        this.guestOs = input.getOsType();
        this.type = input.getMiddlewareType();
        this.classification = input.getClassification();
        this.cpuCount = input.getCpuCount();
        this.setMemoryAsGig(input.getMemoryAsGb());
        this.setDescription(input.getDescription());
        this.setExtraDiskAsGig(input.getExtraDisk());
        if (input.getEnvironmentClass() != EnvironmentClass.u) {
            changeDeployerPassword = true;
        }
    }

    public Vm(Zone zone, OSType guestOs, MiddlewareType type, Classification classification, int cpucount, int memorySize) {
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

    public MiddlewareType getType() {
        return type;
    }

    public void setType(MiddlewareType type) {
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

    public Integer getExtraDisk() {
        return extraDisk;
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

    public void addPuppetFact(String name, Optional<String> value) {
        value.ifPresent(v -> addPuppetFact(name, v));
    }

    public void addPuppetFact(String name, String value) {
        this.customFacts.add(new KeyValue(name, value));
    }

    public void addPuppetFact(FactType fact, Optional<String> value) {
        value.ifPresent(v -> addPuppetFact(fact, v));
    }

    public void addPuppetFact(FactType fact, String value) {
        this.customFacts.add(new KeyValue(fact.name(), value));
    }

    public void setMemoryAsGig(Integer memoryAsGig) {
        if (memoryAsGig != null && memoryAsGig > 0) {
            this.memorySize = memoryAsGig * 1024;
        }
    }

    public void setExtraDiskAsGig(Integer extraDiskasGig) {
        if (extraDiskasGig != null && extraDiskasGig > 0) {
            this.extraDisk = extraDiskasGig;
        }
    }

}