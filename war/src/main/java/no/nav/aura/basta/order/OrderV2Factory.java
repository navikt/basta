package no.nav.aura.basta.order;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import no.nav.aura.basta.Converters;
import no.nav.aura.basta.persistence.*;
import no.nav.aura.basta.util.SerializableFunction;
import no.nav.aura.basta.vmware.orchestrator.request.*;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.Role;
import no.nav.aura.basta.vmware.orchestrator.request.VApp.Site;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.OSType;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.PropertyElement.Type;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import java.net.URI;
import java.util.List;

public class OrderV2Factory {

    private final String currentUser;
    private final URI vmInformationUri;
    private final URI bastaStatusUri;
    private final FasitRestClient fasitRestClient;
    private final Settings settings;
    private final NodeType nodeType;
    private final Order order;

    public OrderV2Factory(Order order, String currentUser, URI vmInformationUri, URI bastaStatusUri, FasitRestClient fasitRestClient) {
        this.order = order;
        this.nodeType = order.getNodeType();
        this.settings = order.getSettings();
        this.currentUser = currentUser;
        this.vmInformationUri = vmInformationUri;
        this.bastaStatusUri = bastaStatusUri;
        this.fasitRestClient = fasitRestClient;
    }

    public ProvisionRequest createProvisionOrder() {
        adaptSettingsBasedOnNodeType(nodeType);
        return createProvisionRequest();
    }

    private ProvisionRequest createProvisionRequest() {
        ProvisionRequest provisionRequest = new ProvisionRequest();
        adaptSettingsBasedOnMiddleWareType(settings.getMiddleWareType());
        provisionRequest.setEnvironmentId(settings.getEnvironmentName());
        provisionRequest.setZone(Converters.orchestratorZoneFromLocal(settings.getZone()));
        provisionRequest.setOrderedBy(currentUser);
        provisionRequest.setOwner(currentUser);
        provisionRequest.setRole(roleFrom(nodeType, settings.getMiddleWareType()));
        provisionRequest.setApplication(settings.getApplicationMapping().getName()); // TODO Remove this when Orchestrator supports applicationGroups. This is only here to preserve backwards compatability. When Roger D. is back from holliday
        provisionRequest.setApplicationMapping(settings.getApplicationMapping().getName());
        provisionRequest.setEnvironmentClass(Converters.orchestratorEnvironmentClassFromLocal(settings.getEnvironmentClass(), settings.isMultisite()).getName());
        provisionRequest.setStatusCallbackUrl(bastaStatusUri);
        provisionRequest.setChangeDeployerPassword(settings.getEnvironmentClass() != EnvironmentClass.u);
        provisionRequest.setResultCallbackUrl(vmInformationUri);

        if(settings.getApplicationMapping().isMappedToApplicationGroup()) {
            provisionRequest.setApplications(settings.getApplicationMapping().getApplications());
        }

        for (int siteIdx = 0; siteIdx < 1; ++siteIdx) {
            provisionRequest.getvApps().add(createVApp(Site.so8));
            if (!nodeType.isDeploymentManager()) {
                if (settings.getEnvironmentClass() == EnvironmentClass.p ||
                        (settings.getEnvironmentClass() == EnvironmentClass.q && settings.isMultisite())) {
                    provisionRequest.getvApps().add(createVApp(Site.u89));
                }
            }
        }
        return provisionRequest;
    }

    private Role roleFrom(NodeType nodeType, MiddleWareType middleWareType) {
        switch (nodeType) {
        case APPLICATION_SERVER:
        case WAS_NODES:
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
            return Role.wps;
        case WAS_DEPLOYMENT_MANAGER:
            return Role.was;
        }
        throw new RuntimeException("Unhandled role for node type " + nodeType + " and application server type " + middleWareType);
    }

    private void adaptSettingsBasedOnNodeType(NodeType nodeType) {
        switch (nodeType) {
        case APPLICATION_SERVER:
        case WAS_NODES:
            // Nothing to do
            break;

        case WAS_DEPLOYMENT_MANAGER:
            // TODO: I only do this to get correct role
            settings.setMiddleWareType(MiddleWareType.wa);
            settings.setApplicationMappingName(Optional.fromNullable(settings.getApplicationMapping().getName()).or("bpm")); // TODO should we have a WAS deployment manager application?
            settings.setServerCount(Optional.fromNullable(settings.getServerCount()).or(1));
            settings.setServerSize(Optional.fromNullable(settings.getServerSize()).or(ServerSize.s));
            break;

        case BPM_DEPLOYMENT_MANAGER:
            // TODO: I only do this to get correct role
            settings.setMiddleWareType(MiddleWareType.wa);
            settings.setApplicationMappingName(Optional.fromNullable(settings.getApplicationMapping().getName()).or("bpm"));
            settings.setServerCount(1);
            settings.setServerSize(Optional.fromNullable(settings.getServerSize()).or(ServerSize.s));
            break;

        case BPM_NODES:
            // TODO: I only do this to get correct role
            settings.setMiddleWareType(MiddleWareType.wa);
            settings.setApplicationMappingName(Optional.fromNullable(settings.getApplicationMapping().getName()).or("bpm"));
            settings.setServerCount(Optional.fromNullable(settings.getServerCount()).or(1));
            settings.setServerSize(Optional.fromNullable(settings.getServerSize()).or(ServerSize.xl));
            break;

        case PLAIN_LINUX:
            settings.setMiddleWareType(MiddleWareType.ap);
            settings.setApplicationMappingName(Optional.fromNullable(settings.getApplicationMapping().getName()).or("PlainLinux"));
            settings.setServerCount(Optional.fromNullable(settings.getServerCount()).or(1));
            settings.setServerSize(Optional.fromNullable(settings.getServerSize()).or(ServerSize.s));
            break;

        default:
            throw new RuntimeException("Unknown node type " + nodeType);
        }
    }

    private void adaptSettingsBasedOnMiddleWareType(MiddleWareType middleWareType) {
        switch (middleWareType) {
        case wa:
            settings.addDisk();
            break;
        case jb:
        case ap:
        default:
            break;
        }
    }


    private VApp createVApp(Site site) {
        List<Vm> vms = Lists.newArrayList();
        for (int vmIdx = 0; vmIdx < settings.getServerCount(); ++vmIdx) {
            List<Disk> disks = Lists.newArrayList();
            if (Optional.fromNullable(settings.getDisks()).isPresent() && settings.getDisks() != 0) {
                disks.add(new Disk(settings.getDisks() * ServerSize.m.externDiskMB));
            }
            Vm vm = new Vm(
                    OSType.rhel60,
                    settings.getMiddleWareType(),
                    settings.getServerSize().cpuCount,
                    settings.getServerSize().ramMB,
                    disks.toArray(new Disk[disks.size()]));
            updateWasAndBpmSettings(vm, vmIdx, site);
            vm.setDmz(false);
            // Vi vet ikke hvor descriptions havner; vi har sett etter dem
            vm.setDescription("");
            vms.add(vm);
        }
        return new VApp(site, null, vms.toArray(new Vm[vms.size()]));
    }

    private void updateWasAndBpmSettings(Vm vm, int vmIdx, Site site) {
        if (settings.getMiddleWareType() == MiddleWareType.wa) {
            String environmentName = settings.getEnvironmentName();
            DomainDO domain = DomainDO.fromFqdn(Converters.domainFqdnFrom(settings.getEnvironmentClass(), settings.getZone()));
            String applicationName = settings.getApplicationMapping().getName();
            List<Fact> facts = Lists.newArrayList();
            String wasType = "mgr";
            if (nodeType == NodeType.WAS_NODES) {
                wasType = "node";
                facts.addAll(createWasApplicationServerFacts(environmentName, domain, applicationName));
            }
            FactType typeFactName = FactType.cloud_app_was_type;
            if (nodeType == NodeType.BPM_DEPLOYMENT_MANAGER) {
                typeFactName = FactType.cloud_app_bpm_type;
                facts.addAll(createBpmDeploymentManagerFacts(environmentName, domain, applicationName));
                facts.add(createBpmServiceUserFact(vmIdx, environmentName, domain, applicationName));
            }
            if (nodeType == NodeType.BPM_NODES) {
                typeFactName = FactType.cloud_app_bpm_type;
                wasType = "node";
                facts.addAll(createBpmNodeFacts(vmIdx, settings.getEnvironmentClass(), environmentName, domain, applicationName, site));
                facts.add(createBpmServiceUserFact(vmIdx, environmentName, domain, applicationName));
            }

            facts.addAll(createWasAdminUserFacts(environmentName, domain, applicationName));
            facts.addAll(createLDAPUserFacts(environmentName, domain, applicationName));
            if (nodeType.isDeploymentManager() && domain.getZone().equals(DomainDO.Zone.SBS)) {
                // All deploymentManagers in SBS should also contain facts for FSS. Oh, the horror.
                facts.addAll(createLDAPUserFactsForFSS(environmentName, domain, applicationName));
            }
            facts.add(new Fact(typeFactName, wasType));
            vm.setCustomFacts(facts);
        }
    }

    private Fact createBpmServiceUserFact(int vmIdx, String environmentName, DomainDO domain, String applicationName) {
        ResourceElement resource = fasitRestClient.getResource(environmentName, settings.getProperty(FasitProperties.BPM_SERVICE_CREDENTIAL_ALIAS).get(), ResourceTypeDO.Credential, domain, applicationName);
        Fact fact = new Fact(FactType.cloud_app_bpm_adminpwd, getProperty(resource, "password"));
        return fact;
    }

    private List<Fact> createWasApplicationServerFacts(String environmentName, DomainDO domain, String applicationName) {
        List<Fact> facts = Lists.newArrayList();
        ResourceElement deploymentManager = fasitRestClient.getResource(environmentName, "wasDmgr", ResourceTypeDO.DeploymentManager, domain, applicationName);
        if (deploymentManager == null) {
            throw new RuntimeException("Domain manager missing for environment " + environmentName + ", domain " + domain + " and application " + applicationName);
        }
        facts.add(new Fact(FactType.cloud_app_was_mgr, getProperty(deploymentManager, "hostname")));
        return facts;
    }

    private List<Fact> createBpmDeploymentManagerFacts(String environmentName, DomainDO domain, String applicationName) {
        List<Fact> facts = Lists.newArrayList();
        ResourceElement commonDataSource = fasitRestClient.getResource(environmentName, settings.getProperty(FasitProperties.BPM_COMMON_DATASOURCE_ALIAS).get(), ResourceTypeDO.DataSource, domain,
                applicationName);
        ResourceElement cellDataSource = fasitRestClient.getResource(environmentName, settings.getProperty(FasitProperties.BPM_CELL_DATASOURCE_ALIAS).get(), ResourceTypeDO.DataSource, domain, applicationName);
        facts.add(new Fact(FactType.cloud_app_bpm_dburl, getProperty(commonDataSource, "url")));
        facts.add(new Fact(FactType.cloud_app_bpm_cmnpwd, getProperty(commonDataSource, "password")));
        facts.add(new Fact(FactType.cloud_app_bpm_cellpwd, getProperty(cellDataSource, "password")));
        return facts;
    }

    private List<Fact> createBpmNodeFacts(int vmIdx, EnvironmentClass environmentClass, String environmentName, DomainDO domain, String applicationName, Site site) {
        List<Fact> facts = Lists.newArrayList();
        ResourceElement deploymentManager = fasitRestClient.getResource(environmentName, "bpmDmgr", ResourceTypeDO.DeploymentManager, domain, applicationName);
        int numberOfExistingNodes = fasitRestClient.getNodeCount(environmentName, applicationName);
        if (deploymentManager == null) {
            throw new RuntimeException("Domain manager missing for environment " + environmentName + ", domain " + domain + " and application " + applicationName);
        }
        ResourceElement commonDataSource = fasitRestClient.getResource(environmentName, settings.getProperty(FasitProperties.BPM_COMMON_DATASOURCE_ALIAS).get(), ResourceTypeDO.DataSource, domain,
                applicationName);
        facts.add(new Fact(FactType.cloud_app_bpm_dburl, getProperty(commonDataSource, "url")));

        try {
            ResourceElement failoverDataSource = fasitRestClient.getResource(environmentName, "bpmFailoverDb", ResourceTypeDO.DataSource, domain, applicationName);
            facts.add(new Fact(FactType.cloud_app_bpm_dbfailoverurl, getProperty(failoverDataSource, "url")));
        } catch (IllegalArgumentException e) {
            facts.add(new Fact(FactType.cloud_app_bpm_dbfailoverurl, ""));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        facts.add(new Fact(FactType.cloud_app_bpm_mgr, getProperty(deploymentManager, "hostname")));
        if (Converters.isMultisite(environmentClass, environmentName)) {
            // Multisite servers ordered, the incrementing of the node numbers are slightly more advanced.
            if (site == Site.u89) {
                facts.add(new Fact(FactType.cloud_app_bpm_node_num, Integer.toString(numberOfExistingNodes + (vmIdx * 2) + 2)));
            } else {
                facts.add(new Fact(FactType.cloud_app_bpm_node_num, Integer.toString(numberOfExistingNodes + (vmIdx * 2) + 1)));
            }
        } else {
            facts.add(new Fact(FactType.cloud_app_bpm_node_num, Integer.toString(numberOfExistingNodes + vmIdx + 1)));
        }
        return facts;
    }

    private List<Fact> createWasAdminUserFacts(String environmentName, DomainDO domain, String applicationName) {
        List<Fact> facts = Lists.newArrayList();
        ResourceElement credential = fasitRestClient.getResource(environmentName,
                settings.getProperty(FasitProperties.WAS_ADMIN_CREDENTIAL_ALIAS).get(), ResourceTypeDO.Credential, domain, applicationName);
        facts.add(new Fact(FactType.cloud_app_was_adminuser, getProperty(credential, "username")));
        facts.add(new Fact(FactType.cloud_app_was_adminpwd, getProperty(credential, "password")));

        return facts;
    }

    private List<Fact> createLDAPUserFacts(String environmentName, DomainDO domain, String applicationName) {
        List<Fact> facts = Lists.newArrayList();
        ResourceElement credential = fasitRestClient.getResource(environmentName,
                settings.getProperty(FasitProperties.LDAP_USER_CREDENTIAL_ALIAS).get(), ResourceTypeDO.Credential, domain, applicationName);
        facts.add(new Fact(FactType.cloud_app_ldap_binduser, getProperty(credential, "username")));
        facts.add(new Fact(FactType.cloud_app_ldap_bindpwd, getProperty(credential, "password")));

        return facts;
    }

    private List<Fact> createLDAPUserFactsForFSS(String environmentName, DomainDO domain, String applicationName) {
        List<Fact> facts = Lists.newArrayList();
        DomainDO mappedDomain = mapZoneFromSBSToFSS(domain);
        ResourceElement credential = fasitRestClient.getResource(environmentName,
                settings.getProperty(FasitProperties.LDAP_USER_CREDENTIAL_ALIAS).get(), ResourceTypeDO.Credential, mappedDomain, applicationName);
        facts.add(new Fact(FactType.cloud_app_ldap_binduser_fss, getProperty(credential, "username")));
        facts.add(new Fact(FactType.cloud_app_ldap_bindpwd_fss, getProperty(credential, "password")));
        return facts;
    }

    private DomainDO mapZoneFromSBSToFSS(DomainDO domainDO) {
        switch (domainDO) {
        case Oera:
            return DomainDO.Adeo;
        case OeraQ:
            return DomainDO.PreProd;
        case OeraT:
            return DomainDO.TestLocal;
        default:
            return domainDO;
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
