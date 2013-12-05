package no.nav.aura.basta.order;

import java.net.URI;
import java.util.List;

import no.nav.aura.basta.Converters;
import no.nav.aura.basta.persistence.ApplicationServerType;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.ServerSize;
import no.nav.aura.basta.rest.SettingsDO;
import no.nav.aura.basta.vmware.orchestrator.request.Disk;
import no.nav.aura.basta.vmware.orchestrator.request.Fact;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.vmware.orchestrator.request.VApp;
import no.nav.aura.basta.vmware.orchestrator.request.VApp.Site;
import no.nav.aura.basta.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.OSType;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class OrderV2Factory {

    private final SettingsDO settings;
    private final String currentUser;
    private final URI vmInformationUri;
    private final URI bastaStatusUri;
    private final FasitRestClient fasitRestClient;

    public OrderV2Factory(SettingsDO settings, String currentUser, URI vmInformationUri, URI bastaStatusUri, FasitRestClient fasitRestClient) {
        this.settings = settings;
        this.currentUser = currentUser;
        this.vmInformationUri = vmInformationUri;
        this.bastaStatusUri = bastaStatusUri;
        this.fasitRestClient = fasitRestClient;
    }

    public ProvisionRequest createOrder() {
        ProvisionRequest provisionRequest = new ProvisionRequest();
        provisionRequest.setEnvironmentId(settings.getEnvironmentName());
        provisionRequest.setZone(Converters.orchestratorZoneFromLocal(settings.getZone()));
        provisionRequest.setOrderedBy(currentUser);
        provisionRequest.setOwner(currentUser);
        provisionRequest.setRole(Converters.roleFromApplicationServerType(settings.getApplicationServerType()));
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

    private VApp createVApp(Site site) {
        List<Vm> vms = Lists.newArrayList();
        for (int vmIdx = 0; vmIdx < settings.getServerCount(); ++vmIdx) {
            List<Disk> disks = Lists.newArrayList();
            if (settings.isDisk()) {
                disks.add(new Disk(ServerSize.m.externDiskMB));
            }
            Vm vm = new Vm(
                    OSType.rhel60,
                    Converters.orchestratorMiddleWareTypeFromLocal(settings.getApplicationServerType()),
                    settings.getServerSize().cpuCount,
                    settings.getServerSize().ramMB,
                    disks.toArray(new Disk[disks.size()]));
            if (settings.getApplicationServerType() == ApplicationServerType.wa) {
                String environmentName = settings.getEnvironmentName();
                DomainDO domain = DomainDO.fromFqdn(Converters.domainFrom(settings.getEnvironmentClass(), settings.getZone()));
                String applicationName = settings.getApplicationName();
                ResourceElement domainManager = fasitRestClient.getResource(environmentName, null, ResourceTypeDO.DeploymentManager, domain, applicationName);
                if (domainManager == null) {
                    throw new RuntimeException("Domain manager missing for environment " + environmentName + ", domain " + domain + " and application " + applicationName);
                }
                vm.setCustomFacts(ImmutableList.of(
                        new Fact("cloud_app_was_mgr", getProperty(domainManager, "hostname")),
                        new Fact("cloud_app_was_type", "node")));
            }
            vm.setDmz(false);
            // TODO ?
            vm.setDescription("");
            vms.add(vm);
        }
        return new VApp(site, null /* TODO ? */, vms.toArray(new Vm[vms.size()]));
    }

    private String getProperty(ResourceElement domainManager, String propertyName) {
        for (PropertyElement property : domainManager.getProperties()) {
            if (property.getName().equals(propertyName)) {
                return property.getValue();
            }
        }
        throw new RuntimeException("Property " + propertyName + " not found for Fasit resource " + domainManager.getAlias());
    }

}
