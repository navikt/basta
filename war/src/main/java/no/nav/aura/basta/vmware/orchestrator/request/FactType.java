package no.nav.aura.basta.vmware.orchestrator.request;

public enum FactType {
    cloud_app_bpm_dburl(false),
    cloud_app_bpm_cmnpwd(true),
    cloud_app_bpm_cellpwd(true),
    cloud_app_bpm_mgr(false),
    cloud_app_bpm_node_num(false),
    cloud_app_bpm_type(false),
    cloud_app_was_type(false),
    cloud_app_was_mgr(false),
    cloud_app_was_adminuser(false),
    cloud_app_was_adminpwd(true),
    cloud_app_bpm_adminpwd(true),
    cloud_app_ldap_binduser(false),
    cloud_app_ldap_bindpwd(true),
    cloud_app_ldap_binduser_fss(false),
    cloud_app_ldap_bindpwd_fss(true);


    private final boolean mask;

    FactType(boolean mask) {
        this.mask = mask;
    }

    public boolean isMask() {
        return mask;
    }
}
