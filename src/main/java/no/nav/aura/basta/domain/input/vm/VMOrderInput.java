package no.nav.aura.basta.domain.input.vm;

import no.nav.aura.basta.backend.fasit.deprecated.payload.Zone;
import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddlewareType;
import no.nav.aura.basta.backend.vmware.orchestrator.OSType;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Input;

import java.util.HashMap;
import java.util.Map;

public class VMOrderInput extends MapOperations implements Input {

    /* VM Order Input */
    public static final String APPLICATION_MAPPING_NAME = "applicationMappingName";
    public static final String MIDDLEWARE_TYPE = "middleWareType"; //Type in orchestrator
    public static final String ENVIRONMENT_CLASS = "environmentClass";
    public static final String ENVIRONMENT_NAME = "environmentName";
    public static final String CLUSTER_NAME = "clusterName";
    public static final String SERVER_COUNT = "serverCount";
    public static final String EXTRA_DISK = "extraDisk";
    public static final String MEMORY = "memory";
    public static final String CPU_COUNT = "cpuCount";
    public static final String ZONE = "zone";
    public static final String NODE_TYPE = "nodeType"; // not used in orchestrator
    public static final String CLASSIFICATION = "classification";
    public static final String DESCRIPTION = "description";
    public static final String OS_TYPE = "osType"; // os in orchestrator
    public static final String IBM_SW = "ibmSw";

    public VMOrderInput(Map<String, String> map) {
        super(map);
    }

    public VMOrderInput() {
        this(new HashMap<String, String>());
    }

    public void addDefaultValueIfNotPresent(String key, String defaultValue) {
        if (!getOptional(key).isPresent()) {
            put(key, defaultValue);
        }
    }

    public String hasIbmSoftware() {
        return get(IBM_SW);
    }

    public void setHasIbmSoftware(String value) {
        put(IBM_SW, value);
    }

    public NodeType getNodeType() {
        NodeType nodeType = getEnumOrNull(NodeType.class, NODE_TYPE);
        return nodeType != null ? nodeType : NodeType.UNKNOWN;
    }

    public void setNodeType(NodeType nodeType) {
        put(NODE_TYPE, nodeType.name());
    }

    public String getApplicationMappingName() {
        return get(APPLICATION_MAPPING_NAME);
    }

    public String getDescription() {
        return get(DESCRIPTION);
    }

    public void setDescription(String description) {
        put(DESCRIPTION, description);
    }

    public void setApplicationMappingName(String applicationMappingName) {
        put(APPLICATION_MAPPING_NAME, applicationMappingName);
    }

    public MiddlewareType getMiddlewareType() {
        return getEnumOrNull(MiddlewareType.class, MIDDLEWARE_TYPE);

    }

    public void setMiddlewareType(MiddlewareType middleWareType) {
        put(MIDDLEWARE_TYPE, middleWareType.name());
    }

    public String getEnvironmentName() {
        return get(ENVIRONMENT_NAME);
    }

    public String getClusterName() {
        return get(CLUSTER_NAME);
    }

    public Zone getZone() {
        return getEnumOrNull(Zone.class, ZONE);
    }

    public EnvironmentClass getEnvironmentClass() {
        return getEnumOrNull(EnvironmentClass.class, ENVIRONMENT_CLASS);
    }

    public Integer getMemoryAsGb() {
        return getIntOrNull(MEMORY);
    }

    public void setMemory(int memSizeInGb) {
        put(MEMORY, memSizeInGb);
    }

    public Integer getCpuCount() {
        return getIntOrNull(CPU_COUNT);
    }

    public void setCpuCount(int cpuCount) {
        put(CPU_COUNT, cpuCount);
    }

    public Integer getServerCount() {
        return getIntOrNull(SERVER_COUNT);
    }

    public void setExtraDisk(int extraDiskInGb) {
        put(EXTRA_DISK, extraDiskInGb);
    }

    public Integer getExtraDisk() {
        return getIntOrNull(EXTRA_DISK);
    }

    public void setServerCount(int serverCount) {
        put(SERVER_COUNT, serverCount);
    }

    public void setEnvironmentName(String environmentName) {
        put(ENVIRONMENT_NAME, environmentName);
    }

    public void setZone(Zone zone) {
        put(ZONE, zone.name());
    }

    public void setEnvironmentClass(EnvironmentClass environmentClass) {
        put(ENVIRONMENT_CLASS, environmentClass.name());
    }

    @Override
    public String getOrderDescription() {
        return getNodeType().name();
    }

    public Classification getClassification() {
        return getEnumOr(Classification.class, CLASSIFICATION, Classification.custom);

    }

    public void setClassification(Classification classification) {
        put(CLASSIFICATION, classification.name());
    }

    public OSType getOsType() {
        return getEnumOr(OSType.class, OS_TYPE, OSType.rhel80);
    }

    public void setOsType(OSType type) {
        put(OS_TYPE, type.name());
    }

    public Domain getDomain() {
        return Domain.findBy(getEnvironmentClass(), getZone());
    }
}
