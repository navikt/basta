package no.nav.aura.basta.domain.vminput;

import com.google.common.collect.Maps;
import no.nav.aura.basta.Converters;
import no.nav.aura.basta.domain.Input;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.ServerSize;
import no.nav.aura.basta.persistence.Zone;
import no.nav.aura.basta.vmware.orchestrator.request.Vm;

public class VMOrderInputResolver {


    /*VM Order Input*/
    public static final String APPLICATION_MAPPING_NAME = "applicationMappingName";
    public static final String MIDDLEWARE_TYPE = "middleWareType";
    public static final String ENVIRONMENT_CLASS = "environmentClass";
    public static final String ENVIRONMENT_NAME = "environmentName";
    public static final String SERVER_COUNT = "serverCount";
    public static final String SERVER_SIZE = "serverSize";
    public static final String ZONE = "zone";
    public static final String DISKS = "disks";
    public static final String XML_CUSTOMIZED = "xmlCustomized";

    /*Data sources*/
    public static final String BPM_COMMON_DATASOURCE_ALIAS = "commonDatasource";
    public static final String BPM_FAILOVER_DATASOURCE_ALIAS = "failoverDatasource";
    public static final String BPM_RECOVERY_DATASOURCE_ALIAS = "recoveryDatasource";
    public static final String BPM_CELL_DATASOURCE_ALIAS = "cellDatasource";

    /*Credentials*/
    public static final String WAS_ADMIN_CREDENTIAL_ALIAS = "wasAdminCredential";
    public static final String BPM_SERVICE_CREDENTIAL_ALIAS = "bpmServiceCredential";
    public static final String LDAP_USER_CREDENTIAL_ALIAS = "ldapUserCredential";
    private final Input input;

    public VMOrderInputResolver(Input input) {
        this.input = input;
        addDefaultValueIfNotPresent(SERVER_COUNT, "1");
        addDefaultValueIfNotPresent(DISKS, "0");
    }

    public VMOrderInputResolver() {
        this.input = new Input(Maps.newTreeMap());
    }

    private void addDefaultValueIfNotPresent(String key, String defaultValue){
        if (!input.getOptional(key).isPresent()){
            input.put(key, defaultValue);
        }

    }

    public String getApplicationMappingName() {
        return input.get(APPLICATION_MAPPING_NAME);
    }

    public void setApplicationMappingName(String applicationMappingName) {
        input.put(APPLICATION_MAPPING_NAME, applicationMappingName);
    }

    public Vm.MiddleWareType getMiddleWareType() {
        return input.getEnumOrNull(Vm.MiddleWareType.class, MIDDLEWARE_TYPE);
    }

    public void setMiddleWareType(Vm.MiddleWareType middleWareType) {
        input.put(MIDDLEWARE_TYPE, middleWareType.name());
    }

    public String getEnvironmentName() {
        return input.get(ENVIRONMENT_NAME);
    }

    public Zone getZone() {
        return input.getEnumOrNull(Zone.class, ZONE);
    }

    public EnvironmentClass getEnvironmentClass() {
        return input.getEnumOrNull(EnvironmentClass.class, ENVIRONMENT_CLASS);
    }

    public boolean isMultisite() {
        return Converters.isMultisite(getEnvironmentClass(), getEnvironmentName());
    }

    public Integer getServerCount() {
        return input.getIntOrNull(SERVER_COUNT);
    }

    public void setServerCount(Integer serverCount) {
        input.put(SERVER_COUNT, serverCount.toString());
    }

    public ServerSize getServerSize() {
        return input.getEnumOrNull(ServerSize.class, SERVER_SIZE);
    }

    public void setServerSize(ServerSize serverSize) {
        input.put(SERVER_SIZE, serverSize.name());
    }

    public void addDisk() {
        Integer disks = input.getIntOrNull(DISKS);
        disks++;
        input.put(DISKS, disks.toString());
    }


    public Integer getDisks() {
        return input.getIntOrNull(DISKS);
    }

    public String getBpmServiceCredential() {
        return input.get(BPM_SERVICE_CREDENTIAL_ALIAS);
    }

    public String getBpmCommonDatasource() {
        return input.get(BPM_COMMON_DATASOURCE_ALIAS);
    }

    public String getCellDatasource() {
        return input.get(BPM_CELL_DATASOURCE_ALIAS);
    }

    public String getWasAdminCredential() {
        return input.get(WAS_ADMIN_CREDENTIAL_ALIAS);
    }

    public String getLdapUserCredential() {
        return input.get(LDAP_USER_CREDENTIAL_ALIAS);
    }

    public String getFailoverDatasource() {
        return input.get(BPM_FAILOVER_DATASOURCE_ALIAS);
    }

    public String getRecoveryDataSource() {
        return input.get(BPM_RECOVERY_DATASOURCE_ALIAS);

    }

    public void setXmlCustomized() {
        input.put(XML_CUSTOMIZED, "1");
    }

    public boolean isXMLCustomized(){
        return input.getOptional(XML_CUSTOMIZED).isPresent();
    }

    public void setEnvironmentName(String environmentName) {
        input.put(ENVIRONMENT_NAME, environmentName);
    }

    public void setZone(Zone zone) {
        input.put(ZONE, zone.name());
    }

    public void setEnvironmentClass(EnvironmentClass environmentClass) {
        input.put(ENVIRONMENT_CLASS, environmentClass.name());
    }

    public void setWasAdminCredential(String wasAdminCredential) {
        input.put(WAS_ADMIN_CREDENTIAL_ALIAS, wasAdminCredential);
    }

    public void setLdapUserCredential(String ldapUserCredential) {
        input.put(LDAP_USER_CREDENTIAL_ALIAS, ldapUserCredential);
    }

    public void setBpmCommonDatasource(String commonDatasource) {
        input.put(BPM_COMMON_DATASOURCE_ALIAS, commonDatasource);
    }

    public void setCellDatasource(String cellDatasource) {
        input.put(BPM_CELL_DATASOURCE_ALIAS, cellDatasource);
    }

    public void setBpmServiceCredential(String bpmServiceCrendential) {
        input.put(BPM_SERVICE_CREDENTIAL_ALIAS, bpmServiceCrendential);
    }

    public void setBpmFailoverDatasource(String failoverDatasource) {
        input.put(BPM_FAILOVER_DATASOURCE_ALIAS, failoverDatasource);
    }

    public void setBpmRecoveryDatasourceAlias(String bpmRecoveryDatasource) {
        input.put(BPM_RECOVERY_DATASOURCE_ALIAS, bpmRecoveryDatasource);
    }
}

