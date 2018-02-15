package no.nav.aura.basta.backend.vmware.orchestrator.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Vm {
    private Boolean removedFromSatellite, removedFromPuppet, removedFromAd;
    private String name;

    public Boolean getRemovedFromAd() {
        return removedFromAd;
    }

    public void setRemovedFromAd(Boolean removedFromAd) {
        this.removedFromAd = removedFromAd;
    }

    public Boolean getRemovedFromSatellite() {
        return removedFromSatellite;
    }

    public void setRemovedFromSatellite(Boolean removedFromSatellite) {
        this.removedFromSatellite = removedFromSatellite;
    }

    public Boolean getRemovedFromPuppet() {
        return removedFromPuppet;
    }

    public void setRemovedFromPuppet(Boolean removedFromPuppet) {
        this.removedFromPuppet = removedFromPuppet;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
