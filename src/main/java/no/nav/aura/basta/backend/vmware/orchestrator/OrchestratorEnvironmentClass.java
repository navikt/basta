package no.nav.aura.basta.backend.vmware.orchestrator;

import java.util.Arrays;
import java.util.List;

import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;

public enum OrchestratorEnvironmentClass {
    utv, test, preprod/* tosite */, qa/* single */, prod;

    private static List<String> multisiteenvironments = Arrays.asList("q0", "q1", "q3", "p");

    /**
     * Get environment considering environment in q
     */
    public static OrchestratorEnvironmentClass convert(VMOrderInput input) {
        EnvironmentClass environmentClass = input.getEnvironmentClass();
        String environment = input.getEnvironmentName();
        MiddlewareType middlewareType = input.getMiddlewareType();

        if (environmentClass.equals(EnvironmentClass.q)) {
            if (isMultisiteEnvironment(environment) || MiddlewareType.containerlinux. equals(middlewareType)) {
                return preprod;
            } else {
                return qa;
            }
        }
        return convertWithoutMultisite(environmentClass);
    }

    /**
     * Get standard mapping
     */
    public static OrchestratorEnvironmentClass convertWithoutMultisite(EnvironmentClass environmentClass) {
        switch (environmentClass) {
        case u:
            return utv;
        case t:
            return test;
        case q:
            return qa;
        case p:
            return prod;
        default:
            throw new IllegalArgumentException("Unknown envionment class " + environmentClass);
        }
    }

    /**
     * check if environment is multisite
     */
    public static boolean isMultisiteEnvironment(String environmentName) {
        return multisiteenvironments.contains(environmentName);
    }

}
