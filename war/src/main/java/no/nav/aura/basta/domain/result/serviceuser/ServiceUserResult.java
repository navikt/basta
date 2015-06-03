package no.nav.aura.basta.domain.result.serviceuser;

import static java.lang.System.getProperty;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.ws.rs.core.UriBuilder;

import no.nav.aura.basta.backend.serviceuser.Domain;
import no.nav.aura.basta.backend.serviceuser.ServiceUserAccount;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.result.Result;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;

public class ServiceUserResult extends MapOperations implements Result {

    private static final String ACCOUNTNAME = "accountname";
    private static final String SECURITYDOMAIN = "securitydomain";
    private static final String FASIT_ID = "fasit_id";
    private static final String ALIAS = "alias";
    private static final String DOMAIN = "domain";
    private static final String TYPE = "type";

    public ServiceUserResult(Map<String, String> map) {
        super(map);
    }

    public void add(ServiceUserAccount userAccount, ResourceElement resource) {
        put(ALIAS, userAccount.getAlias());
        put(DOMAIN, userAccount.getDomain().name());
        put(SECURITYDOMAIN, userAccount.getSecurityDomainFqdn());
        put(ACCOUNTNAME, userAccount.getUserAccountName());
        put(TYPE, resource.getType().name());
        put(FASIT_ID, String.valueOf(resource.getId()));
    }

    @Override
    public List<String> keys() {
        List<String> list = new ArrayList<>();
        list.add(getKey());
        return list;
    }

    public String getKey() {
        return get(ACCOUNTNAME) + "@" + get(SECURITYDOMAIN);
    }

    @Override
    public TreeSet<ResultDO> asResultDO() {
        ResultDO resultDO = new ResultDO(getKey());
        resultDO.getDetails().putAll(map);
        resultDO.addDetail("fasitUrl", getFasitLookupURL());
        TreeSet<ResultDO> set = new TreeSet<>();
        set.add(resultDO);
        return set;
    }

    @Override
    public String getDescription() {
        return get(TYPE);
    }

    private String getFasitId() {
        return get(FASIT_ID);
    }

    public String getFasitLookupURL() {
        try {
            return UriBuilder.fromUri(getProperty("fasit.rest.api.url"))
                    .replacePath("lookup")
                    .queryParam("type", "resource")
                    .queryParam("id", getFasitId())
                    .queryParam("name", get(ALIAS))
                    .build()
                    .toURL()
                    .toString();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Illegal URL?", e);
        }
    }

}
