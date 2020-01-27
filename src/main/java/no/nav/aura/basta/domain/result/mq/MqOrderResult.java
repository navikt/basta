package no.nav.aura.basta.domain.result.mq;

import com.google.common.collect.Lists;
import no.nav.aura.basta.backend.fasit.payload.ResourcePayload;
import no.nav.aura.basta.backend.fasit.payload.ResourceType;
import no.nav.aura.basta.backend.mq.MqChannel;
import no.nav.aura.basta.backend.mq.MqQueue;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.input.mq.MQObjectType;
import no.nav.aura.basta.domain.result.Result;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.basta.util.FasitHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MqOrderResult extends MapOperations implements Result {

    private static final String TYPE = "type";
    private static String ALIAS = "fasitAlias";
    private static String FASIT_ID = "fasitId";
    public static final String FASIT_URL = "fasitUrl";

    public MqOrderResult(Map<String, String> map) {
        super(map);
    }

    public void add(MqQueue queue) {
        put("queueName", queue.getName());
        put("queueAlias", queue.getAlias());
        put("backoutQueue", queue.getBackoutQueueName());
        setType(MQObjectType.Queue);
        // put("queueManager", input.getQueueManager());
    }

    public void setType(MQObjectType type) {
        put(TYPE, type.name());
    }
    private void setType(ResourceType type) {
        put(TYPE, type.name());
    }

    public void add(MqChannel channel) {
        put("channelName", channel.getName());
        setType(MQObjectType.Channel);
    }

    public void add(ResourcePayload fasitResource) {
        setType(fasitResource.type);
        put(ALIAS, fasitResource.alias);
        put(FASIT_ID, fasitResource.id);
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
        resultDO.addDetail(FASIT_URL, FasitHelper.getFasitLookupURL(get(FASIT_ID)));
        HashSet<ResultDO> set = new HashSet<>();
        set.add(resultDO);
        return set;
    }

    @Override
    public String getDescription() {
        return getOptional(TYPE).orElse("unknown");
    }

}
