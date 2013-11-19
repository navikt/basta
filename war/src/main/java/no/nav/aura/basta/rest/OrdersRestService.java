package no.nav.aura.basta.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import no.nav.aura.basta.EnvironmentClass;
import no.nav.aura.basta.User;
import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.requestv1.Disk;
import no.nav.aura.basta.vmware.orchestrator.requestv1.ProvisionRequest;
import no.nav.aura.basta.vmware.orchestrator.requestv1.VApp;
import no.nav.aura.basta.vmware.orchestrator.requestv1.Vm;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Path("/")
public class OrdersRestService {

    @POST
    @Path("/orders")
    @Consumes(MediaType.APPLICATION_JSON)
    public String postOrder(SettingsDO settings, @QueryParam("dryRun") Boolean dryRun) {
        checkAccess(EnvironmentClass.from(settings.getEnvironmentClass()));
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

    private void checkAccess(EnvironmentClass environmentClass) {
        User user = User.getCurrentUser();
        if (!user.getEnvironmentClasses().contains(environmentClass)) {
            throw new UnauthorizedException("User " + user.getName() + " does not have access to environment class " + environmentClass);
        }
    }
}
