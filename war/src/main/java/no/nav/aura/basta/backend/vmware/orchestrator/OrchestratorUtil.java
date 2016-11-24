package no.nav.aura.basta.backend.vmware.orchestrator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class OrchestratorUtil {
    public static List<String> stripFqdnFromHostnames(String... hostnames) {
        
        return Arrays.asList(hostnames).stream()
                .map(hostname -> extractHostName(hostname))
                .collect(Collectors.toList());
    }

    private static String extractHostName(String input) {
        int idx = input.indexOf('.');
        return input.substring(0, idx != -1 ? idx : input.length());
    }
}
