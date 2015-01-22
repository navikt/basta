package no.nav.aura.basta.domain.vminput;


import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import no.nav.aura.basta.domain.Input;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

public class HostnamesInputResolver {

    private final Input input;

    public static final String HOSTNAMES_PROPERTY_KEY = "hostnames";

    public HostnamesInputResolver(Input input) {
        this.input = input;
    }

    private static FluentIterable<String> extractHostnames(String hosts) {
        return FluentIterable.from(Arrays.asList(hosts.split("\\s*,\\s*")))
                       .filter(Predicates.containsPattern("."));
    }

    public static Input asInput(String... hostnames) {
        return Input.single(HOSTNAMES_PROPERTY_KEY, StringUtils.join(hostnames, ","));
    }

    public String[] getHostnames(){
        return extractHostnames(input.getOptional(HOSTNAMES_PROPERTY_KEY).or("")).toArray(String.class);

    }

    public static String[] getHostnames(Input input){
        return extractHostnames(input.getOptional(HOSTNAMES_PROPERTY_KEY).or("")).toArray(String.class);
    }

}
