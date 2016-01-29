package no.nav.aura.basta.domain.result.mq;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.google.common.collect.Lists;

import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.result.Result;
import no.nav.aura.basta.rest.dataobjects.ResultDO;


public class MqOrderResult extends MapOperations implements Result {

	public MqOrderResult(Map<String, String> map) {
		super(map);
    }	

	@Override
    public List<String> keys() {
        return Lists.newArrayList(get("alias"));
    }

    @Override
    public TreeSet<ResultDO> asResultDO() {
        final TreeSet<ResultDO> results = new TreeSet<>();
        final ResultDO result = new ResultDO(get("alias"));
        result.addDetail("queueName", get("queueName"));
        result.addDetail("queueAlias", get("queueAlias"));
        result.addDetail("backoutQueue", get("backoutQueue"));
        result.addDetail("queueManager", get("queueManager"));
        results.add(result);
        return results;
    }
	
    @Override
    public String getDescription() {
        return "Queue";
    }
	
}
