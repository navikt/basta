package no.nav.aura.basta.order;

import static java.util.Arrays.asList;
import no.nav.aura.basta.EnvironmentClass;
import no.nav.aura.basta.rest.SettingsDO.Zone;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class Domain {

    private static ImmutableMap<EnvironmentClass, ImmutableMap<Zone, String>> environmentClassMap = ImmutableMap.of(
            EnvironmentClass.u, Maps.toMap(asList(Zone.values()), Functions.constant("devillo.no")),
            EnvironmentClass.t, ImmutableMap.of(Zone.fss, "test.local", Zone.sbs, "oera-t.local"),
            EnvironmentClass.q, ImmutableMap.of(Zone.fss, "preprod.local", Zone.sbs, "oera-q.local"),
            EnvironmentClass.p, ImmutableMap.of(Zone.fss, "adeo.no", Zone.sbs, "oera.no"));

    public static String from(EnvironmentClass environmentClass, Zone zone) {
        return environmentClassMap.get(environmentClass).get(zone);
    }

}
