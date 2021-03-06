package no.nav.aura.basta.domain.result.vm;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.result.Result;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.basta.util.FasitHelper;

public class VMOrderResult extends MapOperations implements Result {

    public static final String HOSTNAMES_PROPERTY_KEY = "hostname";
    public static final String NODE_TYPE_PROPERTY_KEY = "nodetype";
    public static final String NODE_STATUS_PROPERTY_KEY = "nodestatus";
    public static final String RESULT_URL_PROPERTY_KEY = "resultUrl";
    private static final String DELIMITER = ".";

    public VMOrderResult(Map<String, String> map) {
        super(map);
    }

    public void addHostnameWithStatus(String hostname, ResultStatus resultStatus) {
        String key = getFirstPartOf(hostname);
        put(key + DELIMITER + HOSTNAMES_PROPERTY_KEY, hostname);
        put(key + DELIMITER + NODE_STATUS_PROPERTY_KEY, resultStatus.name());
    }

    public void addHostnameWithStatusAndNodeType(String hostname, ResultStatus resultStatus, NodeType nodeType) {
        String key = getFirstPartOf(hostname);
        addHostnameWithStatus(hostname, resultStatus);
        put(key + DELIMITER + NODE_TYPE_PROPERTY_KEY, nodeType != null ? nodeType.name() : NodeType.UNKNOWN.name());
    }

    private String getVMStatus(String key) {
        return get(key + DELIMITER + NODE_STATUS_PROPERTY_KEY);

    }

    private String getNodeType(String key) {
        return get(key + DELIMITER + NODE_TYPE_PROPERTY_KEY);
    }

    private String getHostname(String key) {
        return get(key + DELIMITER + HOSTNAMES_PROPERTY_KEY);
    }

    private String getFirstPartOf(String string) {
        return string.split("\\.")[0];
    }

    public List<String> hostnames() {
        return keys();
    }

    @Override
    public Set<ResultDO> asResultDO() {
        return getKeys().stream()
                .map(toResultDO())
                .collect(Collectors.toSet());
    }

    @Override
    public String getDescription() {
        return aggregatedNodeType().equals(NodeType.UNKNOWN) ? null : aggregatedNodeType().name();
    }

    private Set<String> getKeys() {
        return map.keySet().stream()
                .map(key -> getFirstPartOf(key))
                .collect(Collectors.toSet());
    }

    private Function<String, ResultDO> toResultDO() {
        return key -> {
            ResultDO resultDO = new ResultDO(getHostname(key));
            resultDO.addDetail(NODE_STATUS_PROPERTY_KEY, getVMStatus(key));
            resultDO.addDetail(RESULT_URL_PROPERTY_KEY, FasitHelper.getFasitLookupURL(getHostname(key)));
            resultDO.addDetail(NODE_TYPE_PROPERTY_KEY, getNodeType(getHostname(key)));
            resultDO.setDescription(getDescription());
            return resultDO;
        };
    }

    @Override
    public List<String> keys() {
        return asResultDO().stream().map(resultDo -> resultDo.getResultName()).collect(Collectors.toList());
    }

    private NodeType aggregatedNodeType() {
        NodeType candidate = null;
        for (NodeType nodeType : allNodeTypes()) {
            if (nodeType != null) {
                if (candidate != null && !candidate.equals(nodeType)) {
                    candidate = NodeType.MULTIPLE;
                } else {
                    candidate = NodeType.MULTIPLE.equals(candidate) ? NodeType.MULTIPLE : nodeType;
                }
            }
        }
        return candidate != null ? candidate : NodeType.UNKNOWN;
    }

    private List<NodeType> allNodeTypes() {
        return getKeys().stream()
                .map(key -> getNodeType(key) == null ? NodeType.UNKNOWN : NodeType.valueOf(getNodeType(key)))
                .collect(Collectors.toList());
    }
}
