package no.nav.aura.basta;

import static java.util.Arrays.asList;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.Zone;
import no.nav.aura.basta.vmware.orchestrator.request.ProvisionRequest.envClass;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class Converters {

    private static ImmutableMap<EnvironmentClass, ImmutableMap<Zone, String>> environmentClassMap = ImmutableMap.of(
            EnvironmentClass.u, Maps.toMap(asList(Zone.values()), Functions.constant("devillo.no")),
            EnvironmentClass.t, ImmutableMap.of(Zone.fss, "test.local", Zone.sbs, "oera-t.local"),
            EnvironmentClass.q, ImmutableMap.of(Zone.fss, "preprod.local", Zone.sbs, "oera-q.local"),
            EnvironmentClass.p, ImmutableMap.of(Zone.fss, "adeo.no", Zone.sbs, "oera.no"));

    public static String domainFrom(EnvironmentClass environmentClass, Zone zone) {
        return environmentClassMap.get(environmentClass).get(zone);
    }

    private static ImmutableMap<EnvironmentClass, envClass> orchestratorEnvironmentClassFromLocalMap = ImmutableMap.of(
            EnvironmentClass.u, envClass.utv, EnvironmentClass.t, envClass.test, EnvironmentClass.q, envClass.preprod, EnvironmentClass.p, envClass.prod);

    public static envClass orchestratorEnvironmentClassFromLocal(EnvironmentClass environmentClass) {
        return orchestratorEnvironmentClassFromLocalMap.get(environmentClass);
    }

}
