package no.nav.aura.basta.backend.vmware.orchestrator;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.util.XmlUtils;


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

    public static String censore(OrchestatorRequest request) {
        String xml = XmlUtils.generateXml(request);
        
        if (request instanceof ProvisionRequest) {
            ProvisionRequest provisionRequest = (ProvisionRequest) request;
            List<String> maskable = provisionRequest.getSecrets();
            for (String secret : maskable) {
                xml = xml.replaceAll(Pattern.quote(secret), "**********");
            }
        }
        return xml;

    }
}
