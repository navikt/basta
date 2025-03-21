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
    FLATCAR_LINUX,
    WINDOWS_APPLICATIONSERVER, 
    WINDOWS_INTERNET_SERVER, 
    MULTIPLE,
    LIBERTY, //used in search
    OPENAM_PROXY, //used in search
    OPENAM_SERVER, //used in search
    DOCKERHOST, //used in search
    UNKNOWN;
}
