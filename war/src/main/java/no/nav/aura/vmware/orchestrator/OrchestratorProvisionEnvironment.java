package no.nav.aura.vmware.orchestrator;

import javax.xml.bind.JAXBException;

import no.nav.aura.vmware.orchestrator.request.Disk;
import no.nav.aura.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.vmware.orchestrator.request.VApp;
import no.nav.aura.vmware.orchestrator.request.Vm;

public class OrchestratorProvisionEnvironment extends AbstractOrchestratorEnvironment {

    private String[] requiredProperties = { "orc-workflow", "orc-url", "orc-username", "orc-password", "name", "size", "zone", "owner", "description", "environmentClass", "application", "guestOs",
            "applicationType", "disk", "expires", "updateEnvConf", "orderedBy", "waitForWorkflow", "createApplication", "changeDeployerUser", "envconftestenv", "puppetEnv", "sateliteEnv",
            "engineeringFileList", "vmCount", "overrideMaintenance" };

    private String[] optionalProperties = { "multiSite" };

    private final String SITE1 = "SO8";
    private final String SITE2 = "U89";

    protected ProvisionRequest orcRequest;

    @Override
    protected void initialize() {

        validateProperties(requiredProperties);

        super.initialize();

        orcRequest = new ProvisionRequest();
        orcRequest.setEnvironmentId(System.getProperty("name"));
        orcRequest.setEnvConfTestEnv(System.getProperty("envconftestenv").equals("true") ? true : false);
        orcRequest.setZone(System.getProperty("zone"));
        orcRequest.setOwner(System.getProperty("owner"));
        orcRequest.setOrderedBy(System.getProperty("orderedBy"));
        orcRequest.setDescription(System.getProperty("description"));
        orcRequest.setEnvironmentClass(System.getProperty("environmentClass"));
        orcRequest.setApplication(System.getProperty("application"));
        orcRequest.setUpdateEnvConf(System.getProperty("updateEnvConf").equals("true") ? true : false);
        orcRequest.setProjectId("MOD");
        orcRequest.setExpires(System.getProperty("expires"));
        orcRequest.setChangeDeployUser(System.getProperty("changeDeployerUser").equals("true") ? true : false);
        orcRequest.setCreateApplication(System.getProperty("createApplication").equals("true") ? true : false);
        orcRequest.setPuppetEnv((System.getProperty("puppetEnv").equalsIgnoreCase(("engineering")) ? "engineering" : null));
        orcRequest.setSatelliteEnv((System.getProperty("sateliteEnv").equalsIgnoreCase(("engineering")) ? "engineering" : null));
        orcRequest.setEngineeringFileList(System.getProperty("engineeringFileList").equals("true") ? true : false);
        orcRequest.setOverrideMaintenance(System.getProperty("overrideMaintenance").equals("true") ? true : false);

        int vmCount = Integer.parseInt(System.getProperty("vmCount"));
        String guestOs = System.getProperty("guestOs");
        String size = System.getProperty("size");
        String applicationType = System.getProperty("applicationType");
        Disk disk = (System.getProperty("disk").equals("none") ? null : new Disk(System.getProperty("disk")));
        boolean openAmEnvironment = System.getProperty("application").equals("openam");

        if (createMultisiteVapps()) {

            log.info("Multisite flag is set, will create " + vmCount + " VM" + (vmCount > 1 ? "s" : "") + " on each site (" + SITE1 + " and " + SITE2 + ")");

            // If creating a multisite vApp, need to sett envClass to preprod so that Orchestrator actually will create a
            // multisite environment.
            // TODO: Avoid doing this??
            // TODO: Enforce multisite if q1, q0 eller p
            if (orcRequest.getEnvironmentClass().equalsIgnoreCase("qa")) {
                orcRequest.setEnvironmentClass("preprod");
            }

            VApp vappSite1 = new VApp(SITE1, "");
            VApp vappSite2 = new VApp(SITE2, "");

            for (int i = 0; i < vmCount; i++) {
                vappSite1.addVm(new Vm(guestOs, size, applicationType, disk));
                vappSite2.addVm(new Vm(guestOs, size, applicationType, disk));
            }

            orcRequest.addVapp(vappSite1);
            orcRequest.addVapp(vappSite2);

        } else {
            log.info("There are " + vmCount + " virtual machine" + (vmCount > 1 ? "s" : "") + " ordered, will create 1 vApp with " + vmCount + " Vms");
            VApp vapp = new VApp(SITE1, ""); // No need to set description on each vApp
            for (int i = 0; i < vmCount; i++) {
                vapp.addVm(new Vm(guestOs, size, applicationType, disk));
            }
            orcRequest.addVapp(vapp);
        }

        if (openAmEnvironment) {
            addReverseProxy(guestOs, size, applicationType, disk, vmCount);
        }

    }

    private void addReverseProxy(String guestOs, String size, String applicationType, Disk disk, int vmCount) {
        log.info("Creating OpenAm environment will need to add Reverse proxy VM to each Vapp");
        for (VApp vApp : orcRequest.getvApps()) {
            for (int i = 0; i < vmCount; i++) {
                vApp.addVm(new Vm(guestOs, size, applicationType, disk, true));
            }
        }
    }

    protected boolean createMultisiteVapps() {
        boolean multisite = System.getProperty("multiSite") != null && System.getProperty("multiSite").equals("true");
        boolean supportedEnv = orcRequest.getEnvironmentClass().equals("qa") || orcRequest.getEnvironmentClass().equals("prod");

        if (multisite && !supportedEnv) {
            log.error("Multisite flag is set but will not have any effect. Multisite is only supported for environmentClass QA");
            throw new UnsupportedOperationException("Multisite " + orcRequest.getEnvironmentClass() + " env is not supported");
        }

        return (supportedEnv && multisite);
    }

    public ProvisionRequest getOrcRequest() {
        return orcRequest;
    }

    public void setOrcRequest(ProvisionRequest orcRequest) {
        this.orcRequest = orcRequest;
    }

    public static void main(String[] args) throws JAXBException {
        OrchestratorProvisionEnvironment provisioner = new OrchestratorProvisionEnvironment();
        provisioner.initialize();
        WorkflowExecutor we = new WorkflowExecutor(provisioner.orcUrl, provisioner.orcUsername, provisioner.orcPassword);
        we.executeWorkflow(provisioner.orcWorkflow, provisioner.orcRequest, provisioner.waitForWorkflow);
        if (!provisioner.getOrcRequest().isUpdateEnvConf()) {
            log.warn("UpdateEnvConf is set to false, will call Orchestrator but the environment will not be registered in env-config");
        }
        if (!provisioner.waitForWorkflow) {
            log.info("Successfully called Orchestrator workflow. Will not wait for reply. However env-config should be updated when the environment is ready.\nFingers crossed...");
        }
    }
}
