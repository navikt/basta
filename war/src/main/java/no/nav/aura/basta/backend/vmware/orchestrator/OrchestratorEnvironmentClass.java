package no.nav.aura.basta.backend.vmware.orchestrator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

import com.google.common.collect.ImmutableMap;

import no.nav.aura.basta.domain.input.EnvironmentClass;


public enum OrchestratorEnvironmentClass {
    utv, test, preprod/* tosite */, qa/* single */, prod;
    
    private static ImmutableMap<EnvironmentClass, OrchestratorEnvironmentClass> standardMapping =
            ImmutableMap.of(
                    EnvironmentClass.u, utv,
                    EnvironmentClass.t, test,
                    EnvironmentClass.q, qa,
                    EnvironmentClass.p, prod);

    private static List<String> multisiteenvironments = Arrays.asList("q0", "q1", "q3", "p");

    /**
     * Get environment considering environment in q
     */
    public static OrchestratorEnvironmentClass convert(EnvironmentClass environmentClass, String environment) {
        if (environmentClass.equals(EnvironmentClass.q)) {
            if (isMultisiteEnvironment(environment)) {
                return preprod;
            } else {
                return qa;
            }
        }
        return from(environmentClass);
    }

    /**
     * Get standard mapping
     */
    public static OrchestratorEnvironmentClass from(EnvironmentClass environmentClass) {
        return standardMapping.get(environmentClass);
    }

    /**
     * check if environment is multisite
     */
    public static boolean isMultisiteEnvironment(String environmentName) {
        return multisiteenvironments.contains(environmentName);
    }

}
