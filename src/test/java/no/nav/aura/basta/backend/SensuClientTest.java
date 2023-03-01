package no.nav.aura.basta.backend;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class SensuClientTest {

    @Test
    public void filtersAndExtractsNameForClientsWithMatchingHostname() {
        String hostnameToMatch = "a69apvl1337.adeo.no";
        ImmutableMap<String, String> match = ImmutableMap.of("name", "match", "address", hostnameToMatch);
        ImmutableMap<String, String> nomatch = ImmutableMap.of("name", "nomatch", "address", "nomatch");
        ArrayList<Map<String, String>> payload = Lists.<Map<String, String>> newArrayList(match, nomatch);
        Set<String> clientNamesWithHostname = SensuClient.getClientNamesWithHostname(payload, hostnameToMatch);
        assertTrue("one should be filtered out", clientNamesWithHostname.size() == 1);
        assertTrue("name was extracted", clientNamesWithHostname.iterator().next().equals("match"));
    }
}