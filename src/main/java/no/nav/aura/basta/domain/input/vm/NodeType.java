package no.nav.aura.basta.domain.input.vm;

public enum NodeType {
    JBOSS, 
    WAS_NODES, 
    WAS9_NODES, 
    WAS_DEPLOYMENT_MANAGER, 
    WAS9_DEPLOYMENT_MANAGER, 
    BPM_DEPLOYMENT_MANAGER, 
    BPM86_DEPLOYMENT_MANAGER,
    BPM_NODES,
    BPM86_NODES,
    PLAIN_LINUX,
    LIGHTWEIGHT_LINUX,
    DEV_TOOLS,
    WILDFLY,
    DOCKERHOST,
    FLATCAR_LINUX,
    WINDOWS_APPLICATIONSERVER, 
    WINDOWS_INTERNET_SERVER, 
    MULTIPLE, 
    UNKNOWN;

    public boolean isDeploymentManager() {
        return this.equals(WAS_DEPLOYMENT_MANAGER) || this.equals(WAS9_DEPLOYMENT_MANAGER) || this.equals
                (BPM_DEPLOYMENT_MANAGER) || this.equals(BPM86_DEPLOYMENT_MANAGER);
    }
}
