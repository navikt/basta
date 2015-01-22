package no.nav.aura.basta.domain.vminput;

import com.google.common.collect.Maps;
import no.nav.aura.basta.domain.Input;
import no.nav.aura.basta.persistence.NodeType;

import java.util.Map;


public class NodeTypeInputResolver {

    public static final String NODE_TYPE = "nodeType";

    public static NodeType getNodeType(Input input) {
        NodeType nodeType = input.getEnumOrNull(NodeType.class, NODE_TYPE);
        return nodeType != null ? nodeType : NodeType.UNKNOWN;
    }

    public static Input asInput(NodeType nodeType) {
        Map<String, String> input = Maps.newHashMap();
        input.put(NodeTypeInputResolver.NODE_TYPE, nodeType.name());
        return new Input(input);
    }

    public static void setNodeType(Input input, NodeType nodeType){
        input.put(NODE_TYPE, nodeType.name());
    }
}
