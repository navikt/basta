package no.nav.aura.basta.vmware.orchestrator;

import java.io.File;
import java.io.IOException;

import no.nav.aura.basta.vmware.TrustStoreHelper;

import org.apache.commons.io.FileUtils;

public class CLIWorkflowExecutor {

    public static void main(String[] args) throws IOException {
        String workflowName = args[0];
        String url = args[1];
        String username = args[2];
        String password = args[3];

        TrustStoreHelper.configureTrustStoreWithProvidedTruststore();
        WorkflowExecutor we = new WorkflowExecutor(url, username, password);
        File requestXmlFile = new File(args[4]);
        we.executeWorkflow(workflowName, FileUtils.readFileToString(requestXmlFile), true);
    }

}
