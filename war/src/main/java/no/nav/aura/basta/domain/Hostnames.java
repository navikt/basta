/*
package no.nav.aura.basta.domain;

import java.util.Arrays;

import no.nav.aura.basta.persistence.Settings;
import org.apache.commons.lang.StringUtils;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;

public abstract class Hostnames {

    public static final String HOSTNAMES_PROPERTY_KEY = "decommissionHosts";

    private Hostnames() {
    }

    public static FluentIterable<String> extractHostnames(Settings settings) {
        return extractHostnames(settings.getProperty(HOSTNAMES_PROPERTY_KEY).get());
    }

    public static FluentIterable<String> extractHostnames(String hosts) {
        return FluentIterable.from(Arrays.asList(hosts.split("\\s*,\\s*")))
                .filter(Predicates.containsPattern("."));
    }

    public static void apply(String[] hostnames, Settings settings) {
        settings.setProperty(HOSTNAMES_PROPERTY_KEY, StringUtils.join(hostnames, ","));
    }

}
*/
