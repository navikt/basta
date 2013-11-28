package no.nav.aura.basta.persistence;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


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

    public Settings() {
    }

    public Settings(Order order, String applicationName, ApplicationServerType applicationServerType, EnvironmentClass environmentClass, String environmentName, int serverCount, ServerSize serverSize,
            Zone zone) {
        this.order = order;
        this.applicationName = applicationName;
        this.applicationServerType = applicationServerType;
        this.environmentClass = environmentClass;
        this.environmentName = environmentName;
        this.serverCount = serverCount;
        this.serverSize = serverSize;
        this.zone = zone;
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

}
