package no.nav.aura.basta.rest.bigip;

import java.util.HashSet;

import com.google.common.collect.Sets;

public class BigIPNamer {
    public static String createPolicyName(String environmentName, String environmentClass) {
        String mappedEnvClass = mapToBigIPNamingStandard(environmentClass);
        return "policy_" + mappedEnvClass + "_" + environmentName + "_https_auto";
    }

    public static String createPoolName(String environmentName, String application, String environmentClass) {
        String mappedEnvClass = mapToBigIPNamingStandard(environmentClass);
        return "pool_" + mappedEnvClass + "_" + application + "_" + environmentName + "_https_auto";
    }

    public static HashSet<String> createRuleNames(String applicationName, String environmentName, String environmentClass) {
        return Sets.newHashSet(createStartsWithRuleName(applicationName, environmentName, environmentClass),
                createEqualsRuleName(applicationName, environmentName, environmentClass));
    }

    public static String createEqualsRuleName(String applicationName, String environmentName, String environmentClass) {
        String mappedEnvClass = mapToBigIPNamingStandard(environmentClass);
        return "prule_" + mappedEnvClass + "_" + applicationName + "_" + environmentName + "_https_eq_auto";
    }

    public static String createStartsWithRuleName(String applicationName, String environmentName, String environmentClass) {
        String mappedEnvClass = mapToBigIPNamingStandard(environmentClass);
        return "prule_" + mappedEnvClass + "_" + applicationName + "_" + environmentName + "_https_sw_auto";
    }

    public static String createHostnameRuleName(String applicationName, String environmentName, String environmentClass) {
        String mappedEnvClass = mapToBigIPNamingStandard(environmentClass);
        return "prule_" + mappedEnvClass + "_" + applicationName + "_" + environmentName + "_https_hostname_auto";
    }

    private static String mapToBigIPNamingStandard(String environmentClass) {
        switch (environmentClass) {
        case "u":
            return "utv";
        case "t":
            return "tst";
        case "q":
            return "pp";
        case "p":
            return "pr";
        default:
            throw new RuntimeException("Unknown environmentclass: " + environmentClass);
        }
    }
}
