package no.nav.aura.basta.persistence;

// TODO Rename; with decommissioning this is more of an OrderType than a NodeType
public enum NodeType {
    APPLICATION_SERVER, WAS_NODES,WAS_DEPLOYMENT_MANAGER, BPM_DEPLOYMENT_MANAGER, BPM_NODES, PLAIN_LINUX;

    public boolean isDeploymentManager(){
        return this.equals(WAS_DEPLOYMENT_MANAGER) || this.equals(BPM_DEPLOYMENT_MANAGER);
    }
}
