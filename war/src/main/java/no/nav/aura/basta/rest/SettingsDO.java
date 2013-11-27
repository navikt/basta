package no.nav.aura.basta.rest;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import no.nav.aura.basta.EnvironmentClass;
import no.nav.aura.basta.persistence.Settings;
import no.nav.aura.basta.util.SerializablePredicate;

import com.google.common.collect.FluentIterable;
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
        utv, test, qa, prod;

        @SuppressWarnings("serial")
        public static EnvironmentClassDO from(final EnvironmentClass environmentClass) {
            return FluentIterable.from(Arrays.asList(values())).firstMatch(new SerializablePredicate<EnvironmentClassDO>() {
                public boolean test(EnvironmentClassDO t) {
                    return t.name().startsWith(environmentClass.name());
                }
            }).get();
        }
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

    public SettingsDO() {
    }

    public SettingsDO(Settings settings) {
        this.serverCount = settings.getServerCount();
        this.serverSize = settings.getServerSize();
        // this.disk = settings.getDi
        this.environmentName = settings.getEnvironmentName();
        this.applicationName = settings.getApplicationName();
        this.environmentClass = EnvironmentClassDO.from(settings.getEnvironmentClass());
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
