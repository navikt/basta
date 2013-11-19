package no.nav.aura.basta.order;

import no.nav.aura.basta.User;
import no.nav.aura.basta.rest.SettingsDO;
import no.nav.aura.basta.rest.SettingsDO.ApplicationServerType;
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
        // TODO multisite
        // TODO changeDeployUser
        // TODO envConfTestEnv
        // TODO engineeringFileList
        // TODO overrideMaintenance
        for (int siteIdx = 0; siteIdx < 1; ++siteIdx) {
            VApp vApp = new VApp();
            vApp.setSite("so8");
            for (int vmIdx = 0; vmIdx < settings.getServerCount(); ++vmIdx) {
                Vm vm = new Vm("rhel60", /* TODO size */"s", settings.getApplicationServerType().name(), Lists.<Disk> newArrayList());
                if (settings.getApplicationServerType() == ApplicationServerType.wa) {
                    // puppetfacts
                }
                // TODO vm.addDisk(disk);
                // TODO dmz
                vApp.addVm(vm);
            }
            provisionRequest.addVapp(vApp);
        }
        return provisionRequest;
    }

}
