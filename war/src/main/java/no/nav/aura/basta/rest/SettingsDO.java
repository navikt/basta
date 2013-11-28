package no.nav.aura.basta.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.basta.persistence.ApplicationServerType;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.ServerSize;
import no.nav.aura.basta.persistence.Settings;
import no.nav.aura.basta.persistence.Zone;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SettingsDO {

    private int serverCount;
    private ServerSize serverSize;
    private boolean disk;
    private String environmentName;
    private String applicationName;
    private EnvironmentClass environmentClass;
    private Zone zone;
    private ApplicationServerType applicationServerType;
    private boolean multisite;

    public SettingsDO() {
    }

    public SettingsDO(Settings settings) {
        this.serverCount = settings.getServerCount();
        this.serverSize = settings.getServerSize();
        // this.disk = settings.getDi
        this.environmentName = settings.getEnvironmentName();
        this.applicationName = settings.getApplicationName();
        this.environmentClass = settings.getEnvironmentClass();
        this.zone = settings.getZone();
        this.applicationServerType = settings.getApplicationServerType();
        // this.multisite = settings.getM
    }

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

    public EnvironmentClass getEnvironmentClass() {
        return environmentClass;
    }

    public void setEnvironmentClass(EnvironmentClass environmentClass) {
        this.environmentClass = environmentClass;
    }

    public ApplicationServerType getApplicationServerType() {
        return applicationServerType;
    }

    public void setApplicationServerType(ApplicationServerType applicationServerType) {
        this.applicationServerType = applicationServerType;
    }

    public boolean isMultisite() {
        return multisite;
    }

    public void setMultisite(boolean multisite) {
        this.multisite = multisite;
    }

}
