package no.nav.aura.basta.backend;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SensuClientTest {

    @Test
    public void filtersAndExtractsNameForClientsWithMatchingHostname() {
        String hostnameToMatch = "a69apvl1337.adeo.no";
        ImmutableMap<String, String> match = ImmutableMap.of("name", "match", "address", hostnameToMatch);
        ImmutableMap<String, String> nomatch = ImmutableMap.of("name", "nomatch", "address", "nomatch");
        ArrayList<Map<String, String>> payload = Lists.<Map<String, String>> newArrayList(match, nomatch);
        Set<String> clientNamesWithHostname = SensuClient.getClientNamesWithHostname(payload, hostnameToMatch);
        assertEquals(1, clientNamesWithHostname.size(), "one should be filtered out");
        assertEquals("match", clientNamesWithHostname.iterator().next(), "name was extracted");
    }
}