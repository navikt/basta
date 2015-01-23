package no.nav.aura.basta.backend.vmware.orchestrator;

import no.nav.aura.basta.TrustStoreHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class AbstractOrchestratorEnvironment {
    private static final Logger log = LoggerFactory.getLogger(AbstractOrchestratorEnvironment.class);

    protected String orcWorkflow;
    protected String orcUsername;
    protected String orcPassword;
    protected String orcUrl;
    protected boolean waitForWorkflow;

    protected void validateProperties(String[] requiredProperties) {
        for (String requiredProperty : requiredProperties) {
            String prop = System.getProperty(requiredProperty);
            if (prop == null) {
                log.error("Missing required property " + requiredProperty);
                throw new RuntimeException(String.format("Missing required property %s: \nThe following properties have to be set: \n %s ", requiredProperty, usage(requiredProperties, null)));
            }
        }
    }

    protected void initialize() {
        orcWorkflow = System.getProperty("orc-workflow");
        orcUsername = System.getProperty("orc-username");
        orcPassword = System.getProperty("orc-password");
        orcUrl = System.getProperty("orc-url");
        waitForWorkflow = System.getProperty("waitForWorkflow").equals("true") ? true : false;
        TrustStoreHelper.configureTrustStoreWithProvidedTruststore();
    }

    public String usage(String[] requiredProperties, String[] optionalProperties) {
        String usage = "";
        for (String requiredProperty : requiredProperties) {
            usage += "-D" + requiredProperty + "\n";
        }
        if (optionalProperties != null) {
            usage += "\n\nOptional properties: ";
            for (String optionalProperty : optionalProperties) {
                usage += "-D" + optionalProperty + "\n";
            }
        }
        return usage;
    }

}
