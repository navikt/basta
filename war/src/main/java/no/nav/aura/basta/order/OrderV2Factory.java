package no.nav.aura.basta.order;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import no.nav.aura.basta.EnvironmentClass;
import no.nav.aura.basta.rest.SettingsDO;
import no.nav.aura.basta.rest.SettingsDO.ApplicationServerType;
import no.nav.aura.basta.rest.SettingsDO.EnvironmentClassDO;
import no.nav.aura.basta.vmware.orchestrator.request.Disk;
import no.nav.aura.basta.vmware.orchestrator.request.Fact;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.vmware.orchestrator.request.VApp;
import no.nav.aura.basta.vmware.orchestrator.request.VApp.Site;
import no.nav.aura.basta.vmware.orchestrator.request.Vm;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.OSType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class OrderV2Factory {

    private final SettingsDO settings;
    private final String currentUser;
    private final URI fasitRestUri;
    private final URI bastaStatusUri;

    public OrderV2Factory(SettingsDO settings, String currentUser, URI fasitRestUri, URI bastaStatusUri) {
        this.settings = settings;
        this.currentUser = currentUser;
        this.fasitRestUri = fasitRestUri;
        this.bastaStatusUri = bastaStatusUri;
    }

    public ProvisionRequest createOrder() {
        ProvisionRequest provisionRequest = new ProvisionRequest();
        provisionRequest.setEnvironmentId(settings.getEnvironmentName());
        provisionRequest.setZone(ProvisionRequest.Zone.valueOf(settings.getZone().name()));
        provisionRequest.setOrderedBy(currentUser);
        provisionRequest.setOwner(currentUser);
        provisionRequest.setApplication(settings.getApplicationName());
        provisionRequest.setEnvironmentClass(ProvisionRequest.envClass.valueOf(settings.getEnvironmentClass().name()));
        provisionRequest.setStatusCallbackUrl(bastaStatusUri);
        provisionRequest.setChangeDeployerPassword(settings.getEnvironmentClass() != EnvironmentClassDO.utv);
        // TODO provisionRequest.setRole(null);
        URI fasitResultUri = UriBuilder.fromUri(fasitRestUri).path("vmware").path("{environment}").path("{domain}").path("{application}").path("vm")
                .buildFromEncodedMap(ImmutableMap.of(
                        "environment", settings.getEnvironmentName(),
                        "domain", Domain.from(EnvironmentClass.from(settings.getEnvironmentClass()), settings.getZone()),
                        "application", settings.getApplicationName()));
        provisionRequest.setResultCallbackUrl(fasitResultUri);
        for (int siteIdx = 0; siteIdx < 1; ++siteIdx) {
            provisionRequest.getvApps().add(createVApp(Site.so8));
            if (settings.getEnvironmentClass() == EnvironmentClassDO.prod ||
                    (settings.getEnvironmentClass() == EnvironmentClassDO.qa && settings.isMultisite())) {
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
                // TODO hva skal sizen være?
                disks.add(new Disk(1024));
            }
            Vm vm = new Vm(
                    OSType.rhel60,
                    MiddleWareType.valueOf(settings.getApplicationServerType().name()),
                    settings.getServerSize().cpuCount,
                    2048 /* TODO hva skal sizen være? */,
                    settings.getServerSize().ramMB,
                    disks.toArray(new Disk[disks.size()]));
            if (settings.getApplicationServerType() == ApplicationServerType.wa) {
                // TODO find shit instead of just declaring it
                vm.setCustomFacts(ImmutableList.of(
                        new Fact("cloud_app_was_mgr", "e34jbsl00995.devillo.no"),
                        new Fact("cloud_app_was_type", "node")));
            }
            vm.setDmz(false);
            // TODO ?
            vm.setDescription("");
            vms.add(vm);
        }
        return new VApp(VApp.Site.valueOf(site.name()), null /* TODO ? */, vms.toArray(new Vm[vms.size()]));
    }
}
