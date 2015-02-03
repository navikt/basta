package no.nav.aura.basta.domain.result.vm;


import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.NodeStatus;
import no.nav.aura.basta.domain.MapOperations;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class VMOrderResult extends MapOperations {

    public static final String HOSTNAMES_PROPERTY_KEY = "hostname";
    public static final String RESULT_STATUS_PROPERTY_KEY = "resultstatus";
    private static final String DELIMITER = ".";

    public VMOrderResult(Map map) {
        super(map);
    }

    public static String getFirstHostName(Order order) {
        return order.getResultAs(VMOrderResult.class).asNodes().first().getHostname();
    }


    public void addHostnameWithStatusAndNodeType(String hostname, NodeStatus nodeStatus) {
        String key = getFirstPartOf(hostname);
        put(key + DELIMITER + HOSTNAMES_PROPERTY_KEY, hostname);
        put(key + DELIMITER + RESULT_STATUS_PROPERTY_KEY, nodeStatus.name());

    }


    private NodeStatus getVMStatus(String key) {
        return getEnumOrNull(NodeStatus.class, key + DELIMITER + RESULT_STATUS_PROPERTY_KEY);
    }



    private String getHostname(String key) {
        return getOptional(key + DELIMITER + HOSTNAMES_PROPERTY_KEY).orNull();
    }

    public TreeSet<VMNode> asNodes() {
        return Sets.newTreeSet(FluentIterable.from(getKeys()).transform(toVMNode()));
    }

    private Function<String, VMNode> toVMNode() {
        return new Function<String, VMNode>() {
            @Override
            public VMNode apply(String key) {return new VMNode(getHostname(key), getVMStatus(key));}
        };
    }

    private Set<String> getKeys() {
        return FluentIterable.from(map.keySet())
                       .transform(new Function<String, String>() {
                           @Override
                           public String apply(String input) {return getFirstPartOf(input);
                           }
                       })
                       .toSet();
    }

    private String getFirstPartOf(String string) {
        return string.split("\\.")[0];
    }


}
