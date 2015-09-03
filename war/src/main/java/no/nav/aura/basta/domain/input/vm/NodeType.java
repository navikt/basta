package no.nav.aura.basta.domain.input.vm;

public enum NodeType {
    JBOSS, WAS_NODES, WAS_DEPLOYMENT_MANAGER, BPM_DEPLOYMENT_MANAGER, BPM_NODES, PLAIN_LINUX, OPENAM_SERVER, WINDOWS_APPLICATIONSERVER, WINDOWS_INTERNET_SERVER, MULTIPLE, UNKNOWN, OPENAM_PROXY;

    public boolean isDeploymentManager() {
        return this.equals(WAS_DEPLOYMENT_MANAGER) || this.equals(BPM_DEPLOYMENT_MANAGER);
    }
}
