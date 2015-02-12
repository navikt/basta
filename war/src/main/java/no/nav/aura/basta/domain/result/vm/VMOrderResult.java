package no.nav.aura.basta.domain.result.vm;


import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import no.nav.aura.basta.domain.input.vm.ResultStatus;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.result.Result;
import no.nav.aura.basta.rest.dataobjects.ResultDO;

import javax.ws.rs.core.UriBuilder;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static java.lang.System.getProperty;

public class VMOrderResult extends MapOperations implements Result {

    public static final String HOSTNAMES_PROPERTY_KEY = "hostname";
    public static final String NODE_STATUS_PROPERTY_KEY = "nodestatus";
    public static final String RESULT_URL_PROPERTY_KEY = "resultUrl";
    private static final String DELIMITER = ".";


    public VMOrderResult(Map map) {
        super(map);
    }


    public void addHostnameWithStatusAndNodeType(String hostname, ResultStatus resultStatus) {
        String key = getFirstPartOf(hostname);
        put(key + DELIMITER + HOSTNAMES_PROPERTY_KEY, hostname);
        put(key + DELIMITER + NODE_STATUS_PROPERTY_KEY, resultStatus.name());

    }

    private String getVMStatus(String key) {
        return getOptional(key + DELIMITER + NODE_STATUS_PROPERTY_KEY).orNull();

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
}
