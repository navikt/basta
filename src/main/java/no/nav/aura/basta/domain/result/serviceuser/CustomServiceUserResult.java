package no.nav.aura.basta.domain.result.serviceuser;

import no.nav.aura.basta.backend.fasit.payload.ResourcePayload;
import no.nav.aura.basta.backend.serviceuser.CustomServiceUserAccount;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.result.Result;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class CustomServiceUserResult extends MapOperations implements Result {

    private static final String ACCOUNTNAME = "accountname";
    private static final String DOMAIN = "domain";
    private static final String TYPE = "type";
    private static final String VAULT_PATH = "vaultpath";

    public CustomServiceUserResult(Map<String, String> map) {
        super(map);
    }

    public void add(CustomServiceUserAccount userAccount, ResourcePayload resource) {
        add(userAccount);
        put(TYPE, resource.type.name());
        put(VAULT_PATH, userAccount.getVaultCredsPath());
    }

    public void add(CustomServiceUserAccount userAccount, ResourceElement resource) {
        add(userAccount);
        put(TYPE, resource.getType().name());
    }


    public void add(CustomServiceUserAccount userAccount) {
        put(DOMAIN, userAccount.getDomain().name());
        put(ACCOUNTNAME, userAccount.getUserAccountName());
    }

    @Override
    public List<String> keys() {
        List<String> list = new ArrayList<>();
        list.add(getKey());
        return list;
    }

    public String getKey() {
        if (getDomain() != null) {
            return get(ACCOUNTNAME) + "@" + getDomain().getFqn();
        }
        return get(ACCOUNTNAME);
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
