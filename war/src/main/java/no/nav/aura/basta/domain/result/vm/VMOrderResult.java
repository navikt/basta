package no.nav.aura.basta.domain.result.vm;


import static java.lang.System.getProperty;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.core.UriBuilder;

import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.result.Result;
import no.nav.aura.basta.rest.dataobjects.ResultDO;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

public class VMOrderResult extends MapOperations implements Result {

    public static final String HOSTNAMES_PROPERTY_KEY = "hostname";
    public static final String NODE_TYPE_PROPERTY_KEY = "nodetype";
    public static final String NODE_STATUS_PROPERTY_KEY = "nodestatus";
    public static final String RESULT_URL_PROPERTY_KEY = "resultUrl";
    private static final String DELIMITER = ".";


    @SuppressWarnings("rawtypes")
    public VMOrderResult(Map map) {
        super(map);
    }


    public void addHostnameWithStatusAndNodeType(String hostname, ResultStatus resultStatus, NodeType nodeType) {
        String key = getFirstPartOf(hostname);
        put(key + DELIMITER + HOSTNAMES_PROPERTY_KEY, hostname);
        put(key + DELIMITER + NODE_STATUS_PROPERTY_KEY, resultStatus.name());
        put(key + DELIMITER + NODE_TYPE_PROPERTY_KEY, nodeType!=null ? nodeType.name() : NodeType.UNKNOWN.name());
    }

    public void addNodeType(String hostname, NodeType nodeType){
        String key = getFirstPartOf(hostname);
        put(key + DELIMITER + NODE_TYPE_PROPERTY_KEY, nodeType.name());
    }

    private String getVMStatus(String key) {
        return getOptional(key + DELIMITER + NODE_STATUS_PROPERTY_KEY).orNull();

    }

    private String getNodeType(String key) {
        return getOptional(key + DELIMITER + NODE_TYPE_PROPERTY_KEY).orNull();
    }

    private String getHostname(String key) {
        return getOptional(key + DELIMITER + HOSTNAMES_PROPERTY_KEY).orNull();
    }


    private String getFirstPartOf(String string) {
        return string.split("\\.")[0];
    }


    @Override
    public TreeSet<ResultDO> asResultDO() {
        return Sets.newTreeSet(FluentIterable.from(getKeys()).transform(toResultDO()));
    }

    @Override
    public String getDescription() {
        return aggregatedNodeType().equals(NodeType.UNKNOWN) ? null : aggregatedNodeType().name();
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

    private Function<String, ResultDO> toResultDO() {
        return new Function<String, ResultDO>() {
            @Override
            public ResultDO apply(String key) {
                ResultDO resultDO = new ResultDO(getHostname(key));
                resultDO.addDetail(NODE_STATUS_PROPERTY_KEY, getVMStatus(key));
                resultDO.addDetail(RESULT_URL_PROPERTY_KEY, getFasitLookupURL(getHostname(key)));
                resultDO.addDetail(NODE_TYPE_PROPERTY_KEY, getNodeType(getHostname(key)));
                resultDO.setDescription(getDescription());
                return resultDO;}
        };
    }

    @Override
    public List<String> keys() {
        return FluentIterable.from(asResultDO()).transform(new Function<ResultDO, String>() {
            @Override
            public String apply(ResultDO input) {
                return input.getResultName();
            }
        }).toList();
    }


    private String getFasitLookupURL(String hostname) {
        try {
            return UriBuilder.fromUri(getProperty("fasit.rest.api.url"))
                           .replacePath("lookup")
                           .queryParam("type", "node")
                           .queryParam("name",hostname)
                           .build()
                           .toURL()
                           .toString();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Illegal URL?", e);
        }
    }



    private NodeType aggregatedNodeType() {
        NodeType candidate = null;
        for (NodeType nodeType :  allNodeTypes()) {
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

    private ImmutableList<NodeType> allNodeTypes() {
        return FluentIterable.from(getKeys()).transform(new Function<String, NodeType>() {
            @Override
            public NodeType apply(String key) {
                return getNodeType(key) == null ? NodeType.UNKNOWN : NodeType.valueOf(getNodeType(key));
            }
        }).toList();
    }
}
