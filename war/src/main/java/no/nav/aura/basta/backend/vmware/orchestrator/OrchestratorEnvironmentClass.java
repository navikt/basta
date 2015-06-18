package no.nav.aura.basta.backend.vmware.orchestrator;

import no.nav.aura.basta.domain.input.EnvironmentClass;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public enum OrchestratorEnvironmentClass {
    utv, test, preprod/* tosite */, qa/* single */, prod;

    private static ImmutableMap<EnvironmentClass, OrchestratorEnvironmentClass> standardMapping =
            ImmutableMap.of(
                    EnvironmentClass.u, utv,
                    EnvironmentClass.t, test,
                    EnvironmentClass.q, qa,
                    EnvironmentClass.p, prod);

    private static ImmutableList<String> multisiteenvironments = ImmutableList.of("q0", "q1", "q3");

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
