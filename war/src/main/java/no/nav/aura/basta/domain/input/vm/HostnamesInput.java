package no.nav.aura.basta.domain.input.vm;


import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;

import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.Input;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Map;

public class HostnamesInput extends MapOperations implements Input {

    public static final String HOSTNAMES_PROPERTY_KEY = "hostnames";
    private static final String NODE_TYPE = "nodeType";

    public HostnamesInput(Map map){
        super(map);
    }

    private   FluentIterable<String> extractHostnames(String hosts) {
        return FluentIterable.from(Arrays.asList(hosts.split("\\s*,\\s*")))
                       .filter(Predicates.containsPattern("."));
    }


    public  HostnamesInput(String... hostnames) {
        super(Maps.newHashMap());
        put(HOSTNAMES_PROPERTY_KEY, StringUtils.join(hostnames, ","));
    }

    public String[] getHostnames(){
        return extractHostnames(getOptional(HOSTNAMES_PROPERTY_KEY).or("")).toArray(String.class);
    }

    public NodeType getNodeType() {
        NodeType nodeType = getEnumOrNull(NodeType.class, NODE_TYPE);
        return nodeType != null ? nodeType : NodeType.UNKNOWN;
    }

    public void setNodeType(NodeType nodeType) {
        put(NODE_TYPE, nodeType.name());
    }

    @Override
    public String getOrderDescription() {
        return getNodeType().name();
    }



}
