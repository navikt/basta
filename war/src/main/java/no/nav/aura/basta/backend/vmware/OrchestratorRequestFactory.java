package no.nav.aura.basta.backend.vmware;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import no.nav.aura.basta.backend.vmware.orchestrator.request.Disk;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Fact;
import no.nav.aura.basta.backend.vmware.orchestrator.request.FactType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest.Role;
import no.nav.aura.basta.backend.vmware.orchestrator.request.VApp;
import no.nav.aura.basta.backend.vmware.orchestrator.request.VApp.Site;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm.MiddleWareType;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm.OSType;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.Converters;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.ServerSize;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.PropertyElement.Type;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class OrchestratorRequestFactory {

    private final String currentUser;
    private final URI vmInformationUri;
    private final URI bastaStatusUri;
    private final FasitRestClient fasitRestClient;
    private final VMOrderInput input;
    private final NodeType nodeType;
    private final Order order;

    public OrchestratorRequestFactory(Order order, String currentUser, URI vmInformationUri, URI bastaStatusUri, FasitRestClient fasitRestClient) {
        this.order = order;
        this.input = order.getInputAs(VMOrderInput.class);
        this.nodeType = input.getNodeType();
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
        adaptSettingsBasedOnMiddleWareType(input.getMiddleWareType());
        provisionRequest.setEnvironmentId(input.getEnvironmentName());
        provisionRequest.setZone(Converters.orchestratorZoneFromLocal(input.getZone()));
        provisionRequest.setOrderedBy(currentUser);
        provisionRequest.setOwner(currentUser);
        provisionRequest.setRole(roleFrom(nodeType, input.getMiddleWareType()));
        provisionRequest.setApplication(input.getApplicationMappingName()); // TODO Remove this when Orchestrator supports
                                                                            // applicationGroups. This is only here to preserve
                                                                            // backwards compatability. When Roger D. is back
                                                                            // from holliday
        provisionRequest.setApplicationMappingName(input.getApplicationMappingName());

        provisionRequest.setEnvironmentClass(Converters.orchestratorEnvironmentClassFromLocal(input.getEnvironmentClass(), input.isMultisite()).getName());
        provisionRequest.setStatusCallbackUrl(bastaStatusUri);
        provisionRequest.setChangeDeployerPassword(input.getEnvironmentClass() != EnvironmentClass.u);
        provisionRequest.setResultCallbackUrl(vmInformationUri);

        createVApps(provisionRequest);

        return provisionRequest;
    }

    private void createVApps(ProvisionRequest provisionRequest) {
        provisionRequest.getvApps().add(createVApp(Site.so8));
        if (!nodeType.isDeploymentManager()) {
            if (input.getEnvironmentClass() == EnvironmentClass.p ||
                    (input.getEnvironmentClass() == EnvironmentClass.q && input.isMultisite())) {
                provisionRequest.getvApps().add(createVApp(Site.u89));
            }
        }
    }

    private Role roleFrom(NodeType nodeType, MiddleWareType middleWareType) {
        switch (nodeType) {
        case JBOSS:
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
        case JBOSS:
        case WAS_NODES:
            // Nothing to do
            break;

        case WAS_DEPLOYMENT_MANAGER:
            // TODO: I only do this to get correct role
            input.setMiddleWareType(MiddleWareType.wa);
            input.setApplicationMappingName(Optional.fromNullable(input.getApplicationMappingName()).or("bpm")); // TODO should
                                                                                                                 // we have a
                                                                                                                 // WAS
                                                                                                                 // deployment
                                                                                                                 // manager
                                                                                                                 // application?
            input.setServerSize(Optional.fromNullable(input.getServerSize()).or(ServerSize.s));
            break;

        case BPM_DEPLOYMENT_MANAGER:
            // TODO: I only do this to get correct role
            input.setMiddleWareType(MiddleWareType.wa);
            input.setApplicationMappingName(Optional.fromNullable(input.getApplicationMappingName()).or("bpm"));
            input.setServerCount(1);
            input.setServerSize(Optional.fromNullable(input.getServerSize()).or(ServerSize.s));
            break;

        case BPM_NODES:
            // TODO: I only do this to get correct role
            input.setMiddleWareType(MiddleWareType.wa);
            input.setApplicationMappingName(Optional.fromNullable(input.getApplicationMappingName()).or("applikasjonsgruppe:esb"));
            input.setServerSize(Optional.fromNullable(input.getServerSize()).or(ServerSize.xl));
            break;

        case PLAIN_LINUX:
            input.setMiddleWareType(MiddleWareType.ap);
            input.setApplicationMappingName(Optional.fromNullable(input.getApplicationMappingName()).or("PlainLinux"));
            input.setServerSize(Optional.fromNullable(input.getServerSize()).or(ServerSize.s));
            break;

        default:
            throw new RuntimeException("Unknown node type " + nodeType);
        }
    }

    private void adaptSettingsBasedOnMiddleWareType(MiddleWareType middleWareType) {
        switch (middleWareType) {
        case wa:
            input.addDisk();
            break;
        case jb:
        case ap:
        default:
            break;
        }
    }

    private VApp createVApp(Site site) {
        List<Vm> vms = Lists.newArrayList();
        for (int vmIdx = 0; vmIdx < input.getServerCount(); ++vmIdx) {
            List<Disk> disks = Lists.newArrayList();
            if (input.getDisks() != 0) {
                disks.add(new Disk(input.getDisks() * ServerSize.m.externDiskMB));
            }
            Vm vm = new Vm(
                    OSType.rhel60,
                    input.getMiddleWareType(),
                    input.getServerSize().cpuCount,
                    input.getServerSize().ramMB,
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
        if (input.getMiddleWareType() == MiddleWareType.wa) {
            String environmentName = input.getEnvironmentName();
            DomainDO domain = DomainDO.fromFqdn(Converters.domainFqdnFrom(input.getEnvironmentClass(), input.getZone()));
            String applicationName = input.getApplicationMappingName();
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
                facts.addAll(createBpmNodeFacts(vmIdx, input.getEnvironmentClass(), environmentName, domain, applicationName, site));
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
        ResourceElement resource = getResource(environmentName, input.getBpmServiceCredential(), ResourceTypeDO.Credential, domain);
        Fact fact = new Fact(FactType.cloud_app_bpm_adminpwd, getProperty(resource, "password"));
        return fact;
    }

    private ResourceElement getResource(String environmentName, String alias, ResourceTypeDO type, DomainDO domain) {
        Collection<ResourceElement> resources = fasitRestClient.findResources(null, environmentName, domain, null, type, alias);
        if (resources.isEmpty()) {
            throw new RuntimeException("Resouce " + alias + " of type " + type + " is not found in " + environmentName + ":" + domain);
        }
        return resources.iterator().next();
    }

    private List<Fact> createWasApplicationServerFacts(String environmentName, DomainDO domain, String applicationName) {
        List<Fact> facts = Lists.newArrayList();
        ResourceElement deploymentManager = getResource(environmentName, "wasDmgr", ResourceTypeDO.DeploymentManager, domain);
        if (deploymentManager == null) {
            throw new RuntimeException("Deployment manager missing for environment " + environmentName + ", domain " + domain + " and application " + applicationName);
        }
        facts.add(new Fact(FactType.cloud_app_was_mgr, getProperty(deploymentManager, "hostname")));
        return facts;
    }

    private List<Fact> createBpmDeploymentManagerFacts(String environmentName, DomainDO domain, String applicationName) {
        List<Fact> facts = Lists.newArrayList();
        ResourceElement commonDataSource = getResource(environmentName, input.getBpmCommonDatasource(), ResourceTypeDO.DataSource, domain);
        ResourceElement cellDataSource = getResource(environmentName, input.getCellDatasource(), ResourceTypeDO.DataSource, domain);
        facts.add(new Fact(FactType.cloud_app_bpm_dburl, getProperty(commonDataSource, "url")));
        facts.add(new Fact(FactType.cloud_app_bpm_cmnpwd, getProperty(commonDataSource, "password")));
        facts.add(new Fact(FactType.cloud_app_bpm_cellpwd, getProperty(cellDataSource, "password")));
        return facts;
    }

    private List<Fact> createBpmNodeFacts(int vmIdx, EnvironmentClass environmentClass, String environmentName, DomainDO domain, String applicationName, Site site) {
        List<Fact> facts = Lists.newArrayList();
        ResourceElement deploymentManager = getResource(environmentName, "bpmDmgr", ResourceTypeDO.DeploymentManager, domain);
        int numberOfExistingNodes = fasitRestClient.getNodeCount(environmentName, applicationName);
        if (deploymentManager == null) {
            throw new RuntimeException("Deployment manager missing for environment " + environmentName + ", domain " + domain + " and application " + applicationName);
        }
        ResourceElement commonDataSource = getResource(environmentName, input.getBpmCommonDatasource(), ResourceTypeDO.DataSource, domain);
        facts.add(new Fact(FactType.cloud_app_bpm_dburl, getProperty(commonDataSource, "url")));

        try {
            ResourceElement failoverDataSource = getResource(environmentName, "bpmFailoverDb", ResourceTypeDO.DataSource, domain);
            facts.add(new Fact(FactType.cloud_app_bpm_dbfailoverurl, getProperty(failoverDataSource, "url")));
        } catch (IllegalArgumentException e) {
            facts.add(new Fact(FactType.cloud_app_bpm_dbfailoverurl, ""));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            ResourceElement recoveryDataSource = getResource(environmentName, "bpmRecoveryDb", ResourceTypeDO.DataSource, domain);
            facts.add(new Fact(FactType.cloud_app_bpm_dbrecoveryurl, getProperty(recoveryDataSource, "url")));
            facts.add(new Fact(FactType.cloud_app_bpm_recpwd, getProperty(recoveryDataSource, "password")));
        } catch (IllegalArgumentException e) {
            facts.add(new Fact(FactType.cloud_app_bpm_dbrecoveryurl, ""));
            facts.add(new Fact(FactType.cloud_app_bpm_recpwd, ""));
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
        ResourceElement credential = getResource(environmentName, input.getWasAdminCredential(), ResourceTypeDO.Credential, domain);
        facts.add(new Fact(FactType.cloud_app_was_adminuser, getProperty(credential, "username")));
        facts.add(new Fact(FactType.cloud_app_was_adminpwd, getProperty(credential, "password")));

        return facts;
    }

    private List<Fact> createLDAPUserFacts(String environmentName, DomainDO domain, String applicationName) {
        List<Fact> facts = Lists.newArrayList();
        ResourceElement credential = getResource(environmentName, input.getLdapUserCredential(), ResourceTypeDO.Credential, domain);
        facts.add(new Fact(FactType.cloud_app_ldap_binduser, getProperty(credential, "username")));
        facts.add(new Fact(FactType.cloud_app_ldap_bindpwd, getProperty(credential, "password")));

        return facts;
    }

    private List<Fact> createLDAPUserFactsForFSS(String environmentName, DomainDO domain, String applicationName) {
        List<Fact> facts = Lists.newArrayList();
        DomainDO mappedDomain = mapZoneFromSBSToFSS(domain);
        ResourceElement credential = getResource(environmentName, input.getLdapUserCredential(), ResourceTypeDO.Credential, mappedDomain);
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
