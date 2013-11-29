package no.nav.aura.basta.persistence;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.nav.aura.basta.rest.SettingsDO;

@Entity
@Table
public class Settings extends ModelEntity {

    @ManyToOne(cascade = CascadeType.MERGE)
    private Order order;
    private String applicationName;
    @Enumerated(EnumType.STRING)
    private ApplicationServerType applicationServerType;
    @Enumerated(EnumType.STRING)
    private EnvironmentClass environmentClass;
    private String environmentName;
    private int serverCount;
    @Enumerated(EnumType.STRING)
    private ServerSize serverSize;
    @Enumerated(EnumType.STRING)
    private Zone zone;
    private boolean disk;
    private boolean multisite;

    public Settings() {
    }

    public Settings(Order order, SettingsDO settings) {
        this.order = order;
        this.applicationName = settings.getApplicationName();
        this.applicationServerType = settings.getApplicationServerType();
        this.environmentClass = settings.getEnvironmentClass();
        this.environmentName = settings.getEnvironmentName();
        this.serverCount = settings.getServerCount();
        this.serverSize = settings.getServerSize();
        this.zone = settings.getZone();
        this.disk = settings.isDisk();
        this.multisite = settings.isMultisite();
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public ApplicationServerType getApplicationServerType() {
        return applicationServerType;
    }

    public void setApplicationServerType(ApplicationServerType applicationServerType) {
        this.applicationServerType = applicationServerType;
    }

    public EnvironmentClass getEnvironmentClass() {
        return environmentClass;
    }

    public void setEnvironmentClass(EnvironmentClass environmentClass) {
        this.environmentClass = environmentClass;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
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

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public boolean isDisk() {
        return disk;
    }

    public void setDisk(boolean disk) {
        this.disk = disk;
    }

    public boolean isMultisite() {
        return multisite;
    }

    public void setMultisite(boolean multisite) {
        this.multisite = multisite;
    }

}
