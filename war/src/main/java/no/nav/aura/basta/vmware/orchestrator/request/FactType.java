package no.nav.aura.basta.vmware.orchestrator.request;

public enum FactType {
    cloud_app_bpm_dburl(false),
    cloud_app_bpm_cmnpwd(true),
    cloud_app_bpm_cellpwd(true),
    cloud_app_bpm_mgr(false),
    cloud_app_bpm_node_num(false),
    cloud_app_bpm_type(false),
    cloud_app_was_type(false),
    cloud_app_was_mgr(false);

    private final boolean mask;

    FactType(boolean mask) {
        this.mask = mask;
    }

    public boolean isMask() {
        return mask;
    }
}
