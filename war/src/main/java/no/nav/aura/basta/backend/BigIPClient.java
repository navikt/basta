package no.nav.aura.basta.backend;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import no.nav.aura.basta.backend.bigip.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;


@Component
public class BigIPClient {
    private static final Logger log = LoggerFactory.getLogger(BigIPClient.class);
    private RestClient restClient;

    private String hostname;
    private String baseUrl;

    public  BigIPClient(){}

    public BigIPClient(String hostname, String username, String password) {
        this.restClient = new RestClient(username, password);
        setHostname(hostname);
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
        this.baseUrl = "https://" + hostname + "/mgmt/tm/ltm";
    }

    public void setCredentials(String username, String password) {
        log.debug("set credentials for {}", username);
        this.restClient = new RestClient(username, password);
    }


    public String getHostname() {
        return this.hostname;
    }

    public Map getPool(String poolName) {
        return restClient.get(baseUrl + "/pool/~AutoProv~" + poolName, Map.class).or(emptyMap());
    }

    public Set<String> getMembers(String pool) {

        final Optional<Map> maybeMembers = restClient.get(baseUrl + "/pool/~AutoProv~" + pool + "/members", Map.class);

        if (!maybeMembers.isPresent()) {
            return Sets.newHashSet();
        }

        List<Map> members = (List<Map>) maybeMembers.get().get("items");
        Set<String> memberNames = Sets.newHashSet();

        for (Map member : members) {
            memberNames.add((String) member.get("name"));
        }

        log.debug("Found members {} in pool {}", memberNames, pool);

        return memberNames;
    }

    public Optional<String> getPoolMemberNameFromIp(String poolName, String ip) {
        final Set<String> members = getMembers(poolName);

        for (String memberName : members) {
            if (memberName.startsWith(ip)) {
                log.debug("Resolved ip {} to member name {} in pool {}", ip, memberName, poolName);
                return Optional.of(memberName);
            }
        }
        log.debug("Could not resolve ip {} to any members {} in pool {}.", ip, members, poolName);
        return Optional.absent();
    }






    public void createHttpsMonitor(String monitorName, String isAliveUrl, int interval, int timeout) {
        final String monitorPayload = createHttpsMonitorPayload(monitorName, isAliveUrl, interval, timeout);
        restClient.post(baseUrl + "/monitor/https", monitorPayload);
        log.debug("Created HTTPS monitor with name {} isAliveUrl={}, interval={} (seconds), timeout={} (seconds)", monitorName, isAliveUrl, interval, timeout);
    }

    private static String createHttpsMonitorPayload(String monitorName, String isAliveUrl, int interval, int timeout) {
        Map<String, Object> monitorPayload = Maps.newHashMap();
        monitorPayload.put("name", monitorName);
        monitorPayload.put("partition", "AutoProv");
        monitorPayload.put("send", createSendString(isAliveUrl));
        monitorPayload.put("recv", "HTTP/1.1 200 OK");
        monitorPayload.put("interval", interval);
        monitorPayload.put("timeout", timeout);

        return new Gson().toJson(monitorPayload);
    }

    private static String createSendString(String isAliveUrl) {
        return format("GET /%s HTTP/1.1\\r\\nHost: bigip.nav.no\\r\\nConnection: Close\\r\\n\\r\\n", isAliveUrl);
    }

    public void createTcpHalfOpenMonitor(String monitorName, int interval, int timeout) {
        final String monitorPayload = createTcpHalfOpenMonitorPayload(monitorName, interval, timeout);
        restClient.post(baseUrl + "/monitor/tcp-half-open", monitorPayload);
        log.debug("Created TCP half open monitor with name {}, interval={} (seconds), timeout={} (seconds)", monitorName, interval, timeout);
    }

    private String createTcpHalfOpenMonitorPayload(String monitorName, int interval, int timeout) {
        Map<String, Object> monitorPayload = Maps.newHashMap();
        monitorPayload.put("name", monitorName);
        monitorPayload.put("partition", "AutoProv");
        monitorPayload.put("interval", interval);
        monitorPayload.put("timeout", timeout);

        return new Gson().toJson(monitorPayload);
    }

    public void setMonitorOnMembersInPool(String monitorName, String poolName) {
        final Set<String> poolMembers = getMembers(poolName);
        for (String poolMember : poolMembers) {
            setMonitorOnMember(monitorName, poolMember, poolName);
        }
    }

    public void setMonitorOnMember(String monitorName, String memberName, String poolName) {
        String setMonitorPayload = "{\"monitor\": \"" + monitorName + "\"}";
        restClient.put(baseUrl + "/pool/~AutoProv~" + poolName + "/members/~AutoProv~" + memberName, setMonitorPayload);
        log.debug("Set monitor {} on member {} in pool {}", monitorName, memberName, poolName);
    }

    public Optional<Map> getVirtualServer(String virtualServerName) {
        return restClient.get(baseUrl + "/virtual/~AutoProv~" + virtualServerName, Map.class);

    }
}