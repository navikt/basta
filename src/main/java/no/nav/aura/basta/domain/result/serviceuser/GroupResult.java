package no.nav.aura.basta.domain.result.serviceuser;

import no.nav.aura.basta.backend.fasit.payload.ResourcePayload;
import no.nav.aura.basta.backend.fasit.payload.ResourceType;
import no.nav.aura.basta.backend.serviceuser.FasitServiceUserAccount;
import no.nav.aura.basta.backend.serviceuser.GroupAccount;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.result.Result;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.basta.util.FasitHelper;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class GroupResult extends MapOperations implements Result {

    private static final String GROUPNAME = "groupname";
    private static final String DOMAIN = "domain";
    private static final String TYPE = "type";

    public GroupResult(Map<String, String> map) {
        super(map);
    }

    public void add(GroupAccount groupAccount) {
        put(GROUPNAME, groupAccount.getName());
        put(DOMAIN, groupAccount.getDomain().name());
        put(TYPE, ResourceType.group.toString());
    }

    @Override
    public List<String> keys() {
        List<String> list = new ArrayList<>();
        list.add(getKey());
        return list;
    }

    public String getKey() {
        if (getDomain() != null) {
            return get(GROUPNAME) + "@" + getDomain().getFqn();
        }
        return get(GROUPNAME);
    }

    public Domain getDomain() {
        return getEnumOrNull(Domain.class, DOMAIN);
    }

    @Override
    public TreeSet<ResultDO> asResultDO() {
        ResultDO resultDO = new ResultDO(getKey());
        resultDO.getDetails().putAll(map);
        TreeSet<ResultDO> set = new TreeSet<>();
        set.add(resultDO);
        return set;
    }

    @Override
    public String getDescription() {
        return get(TYPE);
    }
}
