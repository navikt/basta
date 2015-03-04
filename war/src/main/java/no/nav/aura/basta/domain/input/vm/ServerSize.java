package no.nav.aura.basta.domain.input.vm;

public enum ServerSize {
    s(2048, 10 * 1024, 1), m(4096, 20 * 1024, 2), l(8192, 40 * 1024, 2), xl(16384, 40 * 1024, 4);
    public final int ramMB;
    public int externDiskMB;
    public int cpuCount;

    private ServerSize(int ramMB, int externDiskMB, int cpuCount) {
        this.ramMB = ramMB;
        this.externDiskMB = externDiskMB;
        this.cpuCount = cpuCount;
    }
}