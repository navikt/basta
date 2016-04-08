package no.nav.aura.basta.backend.dns;

import no.nav.aura.basta.backend.dns.menandmice.MenAndMiceExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
public class DnsService {

    private static final Logger logger = LoggerFactory.getLogger(DnsService.class);

    @Inject
    private MenAndMiceExecutor executor;

    public DnsService(){};

    public void setExecutor(MenAndMiceExecutor executor){
        this.executor = executor;
    }

    public DnsService(MenAndMiceExecutor executor) {
        this.executor = executor;
    }

    public List<String> getUsers(){
        String session = executor.login();
        List<String> users = executor.getUsers(session);
        executor.logout(session);
        return users;
    }

    public List<String> getHostNamesFor(String ip){
        String session = executor.login();
        List<String> hostnames = executor.getDnsRecords(session, ip);
        executor.logout(session);
        return hostnames;
    }


}
