package no.nav.aura.basta.domain.input.vm;

public enum NodeType {
    JBOSS, WAS_NODES, WAS_DEPLOYMENT_MANAGER, BPM_DEPLOYMENT_MANAGER, BPM_NODES, PLAIN_LINUX, MULTIPLE, UNKNOWN;

    public boolean isDeploymentManager() {
        return this.equals(WAS_DEPLOYMENT_MANAGER) || this.equals(BPM_DEPLOYMENT_MANAGER);
    }
}
