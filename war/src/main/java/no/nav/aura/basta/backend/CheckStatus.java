package no.nav.aura.basta.backend;

import java.util.List;

import no.nav.aura.basta.vmware.TrustStoreHelper;
import no.nav.aura.basta.vmware.orchestrator.WorkflowExecutor;
import no.nav.generated.vmware.ws.WorkflowTokenAttribute;

public class CheckStatus {

    public static void main(String[] args) {
        TrustStoreHelper.configureTrustStoreWithProvidedTruststore();
        OrchestratorServiceImpl service = new OrchestratorServiceImpl(new WorkflowExecutor("https://a01drvw164.adeo.no:8281/vmware-vmo-webcontrol/webservice", "srvOrchestrator@adeo.no", null));
        List<WorkflowTokenAttribute> status = service.getStatus(
                "8ab64bce4293abcf0142c7fda37d0c23");
        // "8ab64bce43031df701430fe783d10232");
        // "8ab64bce43398f4b01436d172a640165");
        for (WorkflowTokenAttribute attr : status) {
            System.out.println("Status: name = " + attr.getName() + ", type = " + attr.getType() + ", value = " + attr.getValue());
        }
    }

}
