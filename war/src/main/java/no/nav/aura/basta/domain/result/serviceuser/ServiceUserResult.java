package no.nav.aura.basta.domain.result.serviceuser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import no.nav.aura.basta.backend.serviceuser.ServiceUserAccount;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.result.Result;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.basta.util.FasitHelper;
import no.nav.aura.envconfig.client.rest.ResourceElement;

public class ServiceUserResult extends MapOperations implements Result {

    private static final String ACCOUNTNAME = "accountname";
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
        return get(ACCOUNTNAME) + "@" + getDomain().getFqn();
    }

    public Domain getDomain() {
        return getEnumOrNull(Domain.class, DOMAIN);
    }

    @Override
    public TreeSet<ResultDO> asResultDO() {
        ResultDO resultDO = new ResultDO(getKey());
        resultDO.getDetails().putAll(map);
        resultDO.addDetail("fasitUrl", FasitHelper.getFasitLookupURL(get(FASIT_ID), get(ALIAS), "resource"));
        TreeSet<ResultDO> set = new TreeSet<>();
        set.add(resultDO);
        return set;
    }

    @Override
    public String getDescription() {
        return get(TYPE);
    }
}
