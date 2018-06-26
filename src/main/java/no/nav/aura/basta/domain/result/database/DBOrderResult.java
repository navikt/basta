package no.nav.aura.basta.domain.result.database;

import com.google.common.collect.Lists;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.result.Result;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.basta.util.FasitHelper;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class DBOrderResult extends MapOperations implements Result {

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String FASIT_URL = "fasitUrl";
    public static final String FASIT_ALIAS = "fasitAlias";
    public static final String FASIT_ID = "fasitId";
    public static final String OEM_ENDPOINT = "statusUri";
    public static final String NODE_STATUS = "nodestatus";

    public DBOrderResult(Map<String, String> map) {
        super(map);
    }

    @Override
    public List<String> keys() {
        return Lists.newArrayList(get("username"));
    }

    @Override
    public TreeSet<ResultDO> asResultDO() {
        final TreeSet<ResultDO> results = new TreeSet<>();
        final ResultDO result = new ResultDO(get(USERNAME));
        result.addDetail(FASIT_URL, FasitHelper.getFasitLookupURL(get(FASIT_ID)));
        result.addDetail(NODE_STATUS, get(NODE_STATUS));
        results.add(result);

        return results;
    }

    @Override
    public String getDescription() {
        return "Oracle";
    }

}
