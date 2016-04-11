package no.nav.aura.basta.domain.result.bigip;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.result.Result;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.basta.util.FasitHelper;

import com.google.common.collect.Lists;

public class BigIPOrderResult extends MapOperations implements Result {

    public static final String FASIT_URL = "fasitUrl";
    public static final String FASIT_ID = "fasitId";
    public static final String NODE_STATUS = "nodestatus";
    public static final String FASIT_ALIAS = "lbConfig";

    public BigIPOrderResult(Map<String, String> map) {
        super(map);
    }

    @Override
    public List<String> keys() {
        return Lists.newArrayList(FASIT_ALIAS);
    }

    @Override
    public TreeSet<ResultDO> asResultDO() {
        final TreeSet<ResultDO> results = new TreeSet<>();
        final ResultDO result = new ResultDO(FASIT_ALIAS);
        result.addDetail(FASIT_URL, FasitHelper.getFasitLookupURL(get(FASIT_ID), FASIT_ALIAS, "resource"));
        result.addDetail(NODE_STATUS, get(NODE_STATUS));
        results.add(result);

        return results;
    }

    @Override
    public String getDescription() {
        return "BIG-IP Config";
    }

}
