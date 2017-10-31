package no.nav.aura.basta.domain.input.vm;

import java.util.HashMap;
import java.util.Map;

import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.Input;

import org.apache.commons.lang3.StringUtils;

public class HostnamesInput extends MapOperations implements Input {

    public static final String HOSTNAMES_PROPERTY_KEY = "hostnames";
    public static final String NODE_TYPE = "nodeType";

    public HostnamesInput(Map<String, String> map) {
        super(map);
    }

    public HostnamesInput(String... hostnames) {
        super(new HashMap<String, String>());
        put(HOSTNAMES_PROPERTY_KEY, StringUtils.join(hostnames, ","));
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
