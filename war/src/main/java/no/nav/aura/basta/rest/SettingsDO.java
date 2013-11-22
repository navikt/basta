package no.nav.aura.basta.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SettingsDO {

    static public enum ServerSize {
        s(2048, 10 * 1024, 1), m(4096, 20 * 1024, 2), l(8192, 40 * 1024, 2);
        public final int ramMB;
        public int externDiskMB;
        public int cpuCount;

        private ServerSize(int ramMB, int externDiskMB, int cpuCount) {
            this.ramMB = ramMB;
            this.externDiskMB = externDiskMB;
            this.cpuCount = cpuCount;
        }
    }

    static public enum Zone {
        fss, sbs
    }

    static public enum EnvironmentClassDO {
        utv, test, qa, prod
    }

    static public enum ApplicationServerType {
        jb, wa
    }

    private int serverCount;
    private ServerSize serverSize;
    private boolean disk;
    private String environmentName;
    private String applicationName;
    private EnvironmentClassDO environmentClass;
    private Zone zone;
    private ApplicationServerType applicationServerType;
    private boolean multisite;

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

    public EnvironmentClassDO getEnvironmentClass() {
        return environmentClass;
    }

    public void setEnvironmentClass(EnvironmentClassDO environmentClass) {
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
