package no.nav.aura.basta.vmware.orchestrator;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import no.nav.aura.basta.vmware.orchestrator.request.DecomissionRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrchestratorDecomissionEnvironment extends AbstractOrchestratorEnvironment {

    private static final Logger log = LoggerFactory.getLogger(OrchestratorDecomissionEnvironment.class);

    String[] requiredProperties = { "orc-workflow", "orc-url", "orc-username", "orc-password", "vmsToRemove", "waitForWorkflow" };

    private DecomissionRequest orcRequest;

    @Override
    protected void initialize() {

        validateProperties(requiredProperties);

        super.initialize();

        List<String> vms = parseVmLIst(System.getProperty("vmsToRemove"));
        orcRequest = new DecomissionRequest(vms.toArray(new String[0]),null,null);
        StringBuffer info = new StringBuffer(String.format("The following %d VM%s will be removed \n", vms.size(), (vms.size() > 1 ? "s" : "")));
        for (String vm : vms) {
            info.append(vm + "\n");
        }
        log.info(info.toString());
    }

    protected static List<String> parseVmLIst(String vmsToremove) {
        String vmNamePattern = "[a-e]\\d{2}[a-z]{4}\\d{3,5}\\.([a-z]+(-?[a-z]+)?)\\.(local|no)";
        String[] vms = vmsToremove.split(",");

        for (int i = 0; i < vms.length; i++) {
            String fqdn = vms[i].trim().toLowerCase();
            if (!fqdn.matches(vmNamePattern)) {
                throw new IllegalArgumentException(
                        String.format("The Vm name %s does not appear to be on the correct format. Expected pattern is [environment][domainNumber][ap|jb]s[l|w][3-5 digit number].[domainName]\n" +
                                "F.ex: e34jbsl00005.devillo.no, b27apvl001.preprod.local, e34jbsl00001.oera-t.local", fqdn));
            }
            vms[i] = fqdn.split("\\.")[0]; // We are only interested in the shortname of the Vm, f.ex e34jbsl00001
        }
        return Arrays.asList(vms);
    }

    public static void main(String[] args) throws JAXBException {
        OrchestratorDecomissionEnvironment decomissioner = new OrchestratorDecomissionEnvironment();
        decomissioner.initialize();
        WorkflowExecutor orc = new WorkflowExecutor(decomissioner.orcUrl, decomissioner.orcUsername, decomissioner.orcPassword);
        orc.executeWorkflow(decomissioner.orcWorkflow, decomissioner.orcRequest, decomissioner.waitForWorkflow);
        if (!decomissioner.waitForWorkflow) {
            log.info("Successfully called Orchestrator workflow. Will not wait for reply. However env-config should be updated when the environment is ready.\nFingers crossed...");
        }
    }
}
