package no.nav.aura.basta.backend.vmware.orchestrator;

import java.util.Arrays;
import java.util.List;

import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.ProvisionRequest;
import no.nav.aura.basta.util.SerializableFunction;
import no.nav.aura.basta.util.XmlUtils;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

public class OrchestratorUtil {
    public static ImmutableList<String> stripFqdnFromHostnames(String[] hostnames) {
        return FluentIterable.from(Arrays.asList(hostnames))
                       .transform(new SerializableFunction<String, String>() {
                           public String process(String input) {
                               int idx = input.indexOf('.');
                               return input.substring(0, idx != -1 ? idx : input.length());
                           }
                       }).toList();
    }

    public static String censore(OrchestatorRequest request) {
        String xml = XmlUtils.generateXml(request);
        
        if (request instanceof ProvisionRequest) {
            ProvisionRequest provisionRequest = (ProvisionRequest) request;
            List<String> maskable = provisionRequest.getSecrets();
            for (String secret : maskable) {
                xml = xml.replaceAll(secret, "**********");
            }
        }
        
        return xml;

    }
}
