package no.nav.aura.basta.backend.vmware.orchestrator;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import no.nav.aura.basta.util.SerializableFunction;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: j116592
 * Date: 26.08.14
 * Time: 15:21
 * To change this template use File | Settings | File Templates.
 */
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
}
