package no.nav.aura.basta.order;

import java.net.URI;
import java.util.List;

import no.nav.aura.basta.Converters;
import no.nav.aura.basta.persistence.BpmProperties;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.NodeType;
import no.nav.aura.basta.persistence.ServerSize;
import no.nav.aura.basta.persistence.Settings;
import no.nav.aura.basta.vmware.orchestrator.request.Disk;
import no.nav.aura.basta.vmware.orchestrator.request.Fact;
import no.nav.aura.basta.vmware.orchestrator.request.FactType;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.Role;
import no.nav.aura.basta.vmware.orchestrator.request.VApp;
import no.nav.aura.basta.vmware.orchestrator.request.VApp.Site;
import no.nav.aura.basta.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.OSType;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.PropertyElement.Type;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class OrderV2Factory {

    private final Settings settings;
    private final String currentUser;
    private final URI vmInformationUri;
    private final URI bastaStatusUri;
    private final FasitRestClient fasitRestClient;

    public OrderV2Factory(Settings settings, String currentUser, URI vmInformationUri, URI bastaStatusUri, FasitRestClient fasitRestClient) {
        this.settings = settings;
        this.currentUser = currentUser;
        this.vmInformationUri = vmInformationUri;
        this.bastaStatusUri = bastaStatusUri;
        this.fasitRestClient = fasitRestClient;
    }

    public ProvisionRequest createOrder() {
        adaptSettings();
        ProvisionRequest provisionRequest = new ProvisionRequest();
        provisionRequest.setEnvironmentId(settings.getEnvironmentName());
        provisionRequest.setZone(Converters.orchestratorZoneFromLocal(settings.getZone()));
        provisionRequest.setOrderedBy(currentUser);
        provisionRequest.setOwner(currentUser);
        provisionRequest.setRole(roleFrom(settings.getOrder().getNodeType(), settings.getMiddleWareType()));
        provisionRequest.setApplication(settings.getApplicationName());
        provisionRequest.setEnvironmentClass(Converters.orchestratorEnvironmentClassFromLocal(settings.getEnvironmentClass()));
        provisionRequest.setStatusCallbackUrl(bastaStatusUri);
        provisionRequest.setChangeDeployerPassword(settings.getEnvironmentClass() != EnvironmentClass.u);
        provisionRequest.setResultCallbackUrl(vmInformationUri);
        for (int siteIdx = 0; siteIdx < 1; ++siteIdx) {
            provisionRequest.getvApps().add(createVApp(Site.so8));
            if (settings.getEnvironmentClass() == EnvironmentClass.p ||
                    (settings.getEnvironmentClass() == EnvironmentClass.q && settings.isMultisite())) {
                provisionRequest.getvApps().add(createVApp(Site.u89));
            }
        }
        return provisionRequest;
    }

    private Role roleFrom(NodeType nodeType, MiddleWareType middleWareType) {
        switch (nodeType) {
        case APPLICATION_SERVER:
        case PLAIN_LINUX:
            switch (middleWareType) {
            case wa:
                return Role.was;
            default:
                return null;
            }
        case BPM_DEPLOYMENT_MANAGER:
            return Role.was;
        case BPM_NODES:
            return Role.bpm;
        case WAS_DEPLOYMENT_MANAGER:
            return Role.was;
        }
        throw new RuntimeException("Unhandled role for node type " + nodeType + " and application server type " + middleWareType);
    }

    private void adaptSettings() {
        switch (settings.getOrder().getNodeType()) {
        case APPLICATION_SERVER:
            // Nothing to do
            break;

        case WAS_DEPLOYMENT_MANAGER:
            // TODO: I only do this to get correct role
            settings.setMiddleWareType(MiddleWareType.wa);
            settings.setApplicationName(Optional.fromNullable(settings.getApplicationName()).or("wasDeploymentManager"));
            settings.setServerCount(Optional.fromNullable(settings.getServerCount()).or(1));
            settings.setServerSize(Optional.fromNullable(settings.getServerSize()).or(ServerSize.s));
            break;

        case BPM_DEPLOYMENT_MANAGER:
            // TODO: I only do this to get correct role
            settings.setMiddleWareType(MiddleWareType.wa);
            settings.setApplicationName(Optional.fromNullable(settings.getApplicationName()).or("bpmDeploymentManager"));
            settings.setServerCount(1);
            settings.setServerSize(Optional.fromNullable(settings.getServerSize()).or(ServerSize.s));
            break;

        case BPM_NODES:
            // TODO: I only do this to get correct role
            settings.setMiddleWareType(MiddleWareType.wa);
            settings.setApplicationName(Optional.fromNullable(settings.getApplicationName()).or("bpm"));
            settings.setServerCount(2);
            settings.setServerSize(Optional.fromNullable(settings.getServerSize()).or(ServerSize.xl));
            break;

        case PLAIN_LINUX:
            settings.setMiddleWareType(MiddleWareType.ap);
            settings.setApplicationName(Optional.fromNullable(settings.getApplicationName()).or("PlainLinux"));
            settings.setServerCount(Optional.fromNullable(settings.getServerCount()).or(1));
            settings.setServerSize(Optional.fromNullable(settings.getServerSize()).or(ServerSize.s));
            break;

        default:
            throw new RuntimeException("Unknown node type " + settings.getOrder().getNodeType());
        }
    }

    private VApp createVApp(Site site) {
        List<Vm> vms = Lists.newArrayList();
        for (int vmIdx = 0; vmIdx < settings.getServerCount(); ++vmIdx) {
            List<Disk> disks = Lists.newArrayList();
            if (settings.isDisk()) {
                disks.add(new Disk(ServerSize.m.externDiskMB));
            }
            Vm vm = new Vm(
                    OSType.rhel60,
                    settings.getMiddleWareType(),
                    settings.getServerSize().cpuCount,
                    settings.getServerSize().ramMB,
                    disks.toArray(new Disk[disks.size()]));
            updateWasAndBpmSettings(vm, vmIdx);
            vm.setDmz(false);
            // TODO ?
            vm.setDescription("");
            vms.add(vm);
        }
        return new VApp(site, null /* TODO ? */, vms.toArray(new Vm[vms.size()]));
    }

    private void updateWasAndBpmSettings(Vm vm, int vmIdx) {
        if (settings.getMiddleWareType() == MiddleWareType.wa) {
            String environmentName = settings.getEnvironmentName();
            DomainDO domain = DomainDO.fromFqdn(Converters.domainFqdnFrom(settings.getEnvironmentClass(), settings.getZone()));
            String applicationName = settings.getApplicationName();
            List<Fact> facts = Lists.newArrayList();
            String wasType = "mgr";
            if (settings.getOrder().getNodeType() == NodeType.APPLICATION_SERVER) {
                ResourceElement deploymentManager = fasitRestClient.getResource(environmentName, "wasDmgr", ResourceTypeDO.DeploymentManager, domain, applicationName);
                if (deploymentManager == null) {
                    throw new RuntimeException("Domain manager missing for environment " + environmentName + ", domain " + domain + " and application " + applicationName);
                }
                facts.add(new Fact(FactType.cloud_app_was_mgr, getProperty(deploymentManager, "hostname")));
                wasType = "node";
            }
            FactType typeFactName = FactType.cloud_app_was_type;
            if (settings.getOrder().getNodeType() == NodeType.BPM_DEPLOYMENT_MANAGER) {
                typeFactName = FactType.cloud_app_bpm_type;
                ResourceElement commonDataSource = fasitRestClient.getResource(environmentName, settings.getProperty(BpmProperties.BPM_COMMON_DATASOURCE_ALIAS).get(), ResourceTypeDO.DataSource, domain,
                        applicationName);
                ResourceElement cellDataSource = fasitRestClient.getResource(environmentName, settings.getProperty(BpmProperties.BPM_CELL_DATASOURCE_ALIAS).get(), ResourceTypeDO.DataSource, domain, applicationName);
                facts.add(new Fact(FactType.cloud_app_bpm_dburl, getProperty(commonDataSource, "url")));
                facts.add(new Fact(FactType.cloud_app_bpm_cmnpwd, getProperty(commonDataSource, "password")));
                facts.add(new Fact(FactType.cloud_app_bpm_cellpwd, getProperty(cellDataSource, "password")));
            }
            if (settings.getOrder().getNodeType() == NodeType.BPM_NODES) {
                typeFactName = FactType.cloud_app_bpm_type;
                wasType = "node";
                ResourceElement deploymentManager = fasitRestClient.getResource(environmentName, "bpmDmgr", ResourceTypeDO.DeploymentManager, domain, applicationName);
                if (deploymentManager == null) {
                    throw new RuntimeException("Domain manager missing for environment " + environmentName + ", domain " + domain + " and application " + applicationName);
                }
                facts.add(new Fact(FactType.cloud_app_bpm_mgr, getProperty(deploymentManager, "hostname")));
                facts.add(new Fact(FactType.cloud_app_bpm_node_num, Integer.toString(vmIdx + 1)));
            }
            facts.add(new Fact(typeFactName, wasType));
            vm.setCustomFacts(facts);
        }
    }

    private String getProperty(ResourceElement resource, String propertyName) {
        for (PropertyElement property : resource.getProperties()) {
            if (property.getName().equals(propertyName)) {
                if (property.getType() == Type.SECRET) {
                    return fasitRestClient.getSecret(property.getRef());
                }
                return property.getValue();
            }
        }
        throw new RuntimeException("Property " + propertyName + " not found for Fasit resource " + resource.getAlias());
    }

}
