package no.nav.aura.bestillingsweb.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SettingsDO {

    static public enum ServerSize {
        s, m, l
    }

    static public enum Zone {
        fss, sbs
    }

    private int serverCount;
    private ServerSize serverSize;
    private boolean disk;
    private String environmentName;
    private String applicationName;
    private String environmentClass;
    private Zone zone;

    public int getServerCount() {
        return serverCount;
    }

    public void setServerCount(int serverCount) {
        this.serverCount = serverCount;
    }

    public ServerSize getServerSize() {
        return serverSize;
    }

    public void setServerSize(ServerSize serverSize) {
        this.serverSize = serverSize;
    }

    public boolean isDisk() {
        return disk;
    }

    public void setDisk(boolean disk) {
        this.disk = disk;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String application) {
        this.applicationName = application;
    }

    public String getEnvironmentClass() {
        return environmentClass;
    }

    public void setEnvironmentClass(String environmentClass) {
        this.environmentClass = environmentClass;
    }

}
