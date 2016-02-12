package no.nav.aura.basta.domain.result.mq;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import no.nav.aura.basta.backend.mq.MqChannel;
import no.nav.aura.basta.backend.mq.MqQueue;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.result.Result;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.basta.util.FasitHelper;
import no.nav.aura.envconfig.client.rest.ResourceElement;

public class MqOrderResult extends MapOperations implements Result {

    private static final String TYPE = "type";
    private static String ALIAS = "fasitAlias";
    private static String FASIT_ID = "fasitId";

    public MqOrderResult(Map<String, String> map) {
        super(map);
    }

    public void add(MqQueue queue) {
        put("queueName", queue.getName());
        put("queueAlias", queue.getAlias());
        put("backoutQueue", queue.getBackoutQueueName());
        put(TYPE, "Queue");
        // put("queueManager", input.getQueueManager());
    }

    public void add(MqChannel channel) {
        put("channelName", channel.getName());
        put(TYPE, "channel");
    }

    public void add(ResourceElement fasitResource) {
        put(ALIAS, fasitResource.getAlias());
        put(FASIT_ID, String.valueOf(fasitResource.getId()));
    }

    @Override
    public List<String> keys() {
        return Lists.newArrayList(get(ALIAS));
    }

    public String getKey() {
        return getOptional(ALIAS).orElse("unknown");
    }

    @Override
    public Set<ResultDO> asResultDO() {
        ResultDO resultDO = new ResultDO(getKey());
        resultDO.getDetails().putAll(map);
        resultDO.addDetail("fasitUrl", FasitHelper.getFasitLookupURL(get(FASIT_ID), get(ALIAS), "resource"));
        HashSet<ResultDO> set = new HashSet<>();
        set.add(resultDO);
        return set;
    }

    @Override
    public String getDescription() {
        return getOptional(TYPE).orElse("unknown");
    }

}
