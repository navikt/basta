package no.nav.aura.basta.domain.input.vm;

import java.util.HashMap;
import java.util.Map;

import no.nav.aura.basta.backend.vmware.orchestrator.Classification;
import no.nav.aura.basta.backend.vmware.orchestrator.MiddleWareType;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.Input;
import no.nav.aura.basta.domain.input.Zone;

public class VMOrderInput extends MapOperations implements Input {

	/* VM Order Input */
    public static final String APPLICATION_MAPPING_NAME = "applicationMappingName";
    public static final String MIDDLEWARE_TYPE = "middleWareType";
    public static final String ENVIRONMENT_CLASS = "environmentClass";
    public static final String ENVIRONMENT_NAME = "environmentName";
    public static final String SERVER_COUNT = "serverCount";
    public static final String SERVER_SIZE = "serverSize";
    public static final String EXTRA_DISK = "extraDisk";
    public static final String MEMORY = "memory";
    public static final String CPU_COUNT = "cpuCount";
    public static final String ZONE = "zone";
    public static final String DISKS = "disks";
    public static final String XML_CUSTOMIZED = "xmlCustomized";
    public static final String NODE_TYPE = "nodeType";
    public static final String CLASSIFICATION = "classification";
    public static final String DESCRIPTION = "description";

    /* Data sources */
    public static final String BPM_COMMON_DATASOURCE_ALIAS = "commonDatasource";
    public static final String BPM_FAILOVER_DATASOURCE_ALIAS = "failoverDatasource";
    public static final String BPM_RECOVERY_DATASOURCE_ALIAS = "recoveryDatasource";
    public static final String BPM_CELL_DATASOURCE_ALIAS = "cellDatasource";

    /* Credentials */
    public static final String WAS_ADMIN_CREDENTIAL_ALIAS = "wasAdminCredential";
    public static final String BPM_SERVICE_CREDENTIAL_ALIAS = "bpmServiceCredential";
    public static final String LDAP_USER_CREDENTIAL_ALIAS = "ldapUserCredential";

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

    public void setApplicationMappingName(String applicationMappingName) {
        put(APPLICATION_MAPPING_NAME, applicationMappingName);
    }

    public MiddleWareType getMiddleWareType() {
        return getEnumOrNull(MiddleWareType.class, MIDDLEWARE_TYPE);
    }

    public void setMiddleWareType(MiddleWareType middleWareType) {
        put(MIDDLEWARE_TYPE, middleWareType.name());
    }

    public String getEnvironmentName() {
        return get(ENVIRONMENT_NAME);
    }

    public Zone getZone() {
        return getEnumOrNull(Zone.class, ZONE);
    }

    public EnvironmentClass getEnvironmentClass() {
        return getEnumOrNull(EnvironmentClass.class, ENVIRONMENT_CLASS);
    }

    public boolean isMultisite() {
        NodeType nodeType = getNodeType();
        if (nodeType.isDeploymentManager() || nodeType == NodeType.PLAIN_LINUX) {
            return false;
        }
        return Converters.isMultisite(getEnvironmentClass(), getEnvironmentName());
    }

    public Integer getMemory() {
        return getIntOrNull(MEMORY);
    }

    public void setMemory(int memSizeInMb){
        put(MEMORY, memSizeInMb);
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

    public void setExtraDisk(int extraDiskInMb) {
        put(EXTRA_DISK, extraDiskInMb);
    }

    public Integer getExtraDisk() {
        return getIntOrNull(EXTRA_DISK);
    }

    public void setServerCount(int serverCount) {
        put(SERVER_COUNT, serverCount);
    }

    public ServerSize getServerSize() {
        return getEnumOrNull(ServerSize.class, SERVER_SIZE);
    }

    public void setServerSize(ServerSize serverSize) {
        put(SERVER_SIZE, serverSize.name());
    }

    public void addDisk() {
        Integer disks = getIntOrNull(DISKS);
        disks++;
        put(DISKS, disks.toString());
    }

    public Integer getDisks() {
        return getIntOrNull(DISKS);
    }

    public String getBpmServiceCredential() {
        return get(BPM_SERVICE_CREDENTIAL_ALIAS);
    }

    public String getBpmCommonDatasource() {
        return get(BPM_COMMON_DATASOURCE_ALIAS);
    }

    public String getCellDatasource() {
        return get(BPM_CELL_DATASOURCE_ALIAS);
    }

    public String getWasAdminCredential() {
        return get(WAS_ADMIN_CREDENTIAL_ALIAS);
    }

    public String getLdapUserCredential() {
        return get(LDAP_USER_CREDENTIAL_ALIAS);
    }

    public String getFailoverDatasource() {
        return get(BPM_FAILOVER_DATASOURCE_ALIAS);
    }

    public String getRecoveryDataSource() {
        return get(BPM_RECOVERY_DATASOURCE_ALIAS);

    }

    public void setXmlCustomized() {
        put(XML_CUSTOMIZED, "1");
    }

    public boolean isXMLCustomized() {
        return getOptional(XML_CUSTOMIZED).isPresent();
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

    public void setWasAdminCredential(String wasAdminCredential) {
        put(WAS_ADMIN_CREDENTIAL_ALIAS, wasAdminCredential);
    }

    public void setLdapUserCredential(String ldapUserCredential) {
        put(LDAP_USER_CREDENTIAL_ALIAS, ldapUserCredential);
    }

    public void setBpmCommonDatasource(String commonDatasource) {
        put(BPM_COMMON_DATASOURCE_ALIAS, commonDatasource);
    }

    public void setCellDatasource(String cellDatasource) {
        put(BPM_CELL_DATASOURCE_ALIAS, cellDatasource);
    }

    public void setBpmServiceCredential(String bpmServiceCrendential) {
        put(BPM_SERVICE_CREDENTIAL_ALIAS, bpmServiceCrendential);
    }

    public void setBpmFailoverDatasource(String failoverDatasource) {
        put(BPM_FAILOVER_DATASOURCE_ALIAS, failoverDatasource);
    }

    public void setBpmRecoveryDatasourceAlias(String bpmRecoveryDatasource) {
        put(BPM_RECOVERY_DATASOURCE_ALIAS, bpmRecoveryDatasource);
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
}
