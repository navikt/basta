package no.nav.aura.basta.domain.input.vm;

public enum NodeType {
    JBOSS, 
    WAS_NODES, 
    WAS9_NODES, 
    WAS_DEPLOYMENT_MANAGER, 
    WAS9_DEPLOYMENT_MANAGER, 
    BPM_DEPLOYMENT_MANAGER, 
    BPM9_DEPLOYMENT_MANAGER, 
    BPM_NODES,
    BPM9_NODES,
    PLAIN_LINUX,
    DEV_TOOLS,
    DOCKERHOST,
    OPENAM_SERVER,
    LIBERTY, 
    WILDFLY, 
    WINDOWS_APPLICATIONSERVER, 
    WINDOWS_INTERNET_SERVER, 
    MULTIPLE, 
    UNKNOWN, 
    OPENAM_PROXY;

    public boolean isDeploymentManager() {
        return this.equals(WAS_DEPLOYMENT_MANAGER) || this.equals(WAS9_DEPLOYMENT_MANAGER) || this.equals(BPM_DEPLOYMENT_MANAGER) || this.equals(BPM9_DEPLOYMENT_MANAGER);
    }
}