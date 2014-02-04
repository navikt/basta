package no.nav.aura.basta.persistence;

import java.util.Arrays;

import no.nav.aura.basta.rest.OrderDetailsDO;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;

public abstract class DecommissionProperties {

    public static final String DECOMMISSION_HOSTS_PROPERTY_KEY = "decommissionHosts";

    private DecommissionProperties() {
    }

    public static FluentIterable<String> extractHostnames(String hosts) {
        return FluentIterable.from(Arrays.asList(hosts.split("\\s*,\\s*")))
                .filter(Predicates.containsPattern("."));
    }

    public static void apply(OrderDetailsDO orderDetails, Settings settings) {
        settings.setProperty(DECOMMISSION_HOSTS_PROPERTY_KEY, StringUtils.join(orderDetails.getHostnames(), ","));
    }

}
