package no.nav.aura.basta.order;

import no.nav.aura.basta.User;
import no.nav.aura.basta.rest.SettingsDO;
import no.nav.aura.basta.rest.SettingsDO.ApplicationServerType;
import no.nav.aura.basta.rest.SettingsDO.EnvironmentClassDO;
import no.nav.aura.basta.rest.SettingsDO.ServerSize;
import no.nav.aura.basta.vmware.orchestrator.request.VApp.Site;
import no.nav.aura.basta.vmware.orchestrator.requestv1.Disk;
import no.nav.aura.basta.vmware.orchestrator.requestv1.ProvisionRequest;
import no.nav.aura.basta.vmware.orchestrator.requestv1.VApp;
import no.nav.aura.basta.vmware.orchestrator.requestv1.Vm;

import com.google.common.collect.Lists;

public class OrderV1Factory {

    private SettingsDO settings;

    public OrderV1Factory(SettingsDO settings) {
        this.settings = settings;
    }

    public ProvisionRequest createOrder() {
        ProvisionRequest provisionRequest = new ProvisionRequest();
        provisionRequest.setEnvironmentId(settings.getEnvironmentName());
        provisionRequest.setZone(settings.getZone().name());
        provisionRequest.setOrderedBy(User.getCurrentUser().getName());
        provisionRequest.setOwner(User.getCurrentUser().getName());
        provisionRequest.setApplication(settings.getApplicationName());
        provisionRequest.setEnvironmentClass(settings.getEnvironmentClass().name());
        // TODO new version will change application on other side
        provisionRequest.setCreateApplication(true);
        // TODO future version should not update fasit
        provisionRequest.setUpdateEnvConf(true);
        // TODO changeDeployUser
        // TODO envConfTestEnv
        // TODO engineeringFileList
        // TODO overrideMaintenance
        for (int siteIdx = 0; siteIdx < 1; ++siteIdx) {
            createVApp(provisionRequest, Site.so8);
            if (settings.getEnvironmentClass() == EnvironmentClassDO.prod ||
                    (settings.getEnvironmentClass() == EnvironmentClassDO.qa && settings.isMultisite())) {
                createVApp(provisionRequest, Site.u89);
            }
        }
        return provisionRequest;
    }

    private void createVApp(ProvisionRequest provisionRequest, Site site) {
        VApp vApp = new VApp();
        vApp.setSite(site.name());
        for (int vmIdx = 0; vmIdx < settings.getServerCount(); ++vmIdx) {
            Vm vm = new Vm("rhel60", /* TODO size */"s", settings.getApplicationServerType().name(), Lists.<Disk> newArrayList());
            if (settings.getApplicationServerType() == ApplicationServerType.wa) {
                // TODO puppetfact: cloud_application_dmgr? = e34jbsl00995.devillo.no
            }
            if (settings.isDisk()) {
                vm.addDisk(new Disk(ServerSize.m.name()));
            }
            // TODO vm.addDisk(disk);
            // TODO dmz
            vApp.addVm(vm);
        }
        provisionRequest.addVapp(vApp);
    }

}
