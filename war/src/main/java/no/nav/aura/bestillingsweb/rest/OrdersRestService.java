package no.nav.aura.bestillingsweb.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import no.nav.aura.bestillingsweb.User;
import no.nav.aura.vmware.XmlUtils;
import no.nav.aura.vmware.orchestrator.request.Disk;
import no.nav.aura.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.vmware.orchestrator.request.VApp;
import no.nav.aura.vmware.orchestrator.request.Vm;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Path("/")
public class OrdersRestService {

    @POST
    @Path("/orders")
    @Consumes(MediaType.APPLICATION_JSON)
    public String postOrder(SettingsDO settings, @QueryParam("dryRun") Boolean dryRun) {
        System.out.println("Content: " + ToStringBuilder.reflectionToString(settings) + ", dryRun: " + dryRun);
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
                Vm vm = new Vm("rhel60", /* TODO size */"s", "ap", Lists.<Disk> newArrayList());
                // TODO vm.addDisk(disk);
                // TODO dmz
                vApp.addVm(vm);
            }
            provisionRequest.addVapp(vApp);
        }
        try {
            return XmlUtils.prettyFormat(XmlUtils.generateXml(provisionRequest), 2);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
