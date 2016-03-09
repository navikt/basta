package no.nav.aura.basta.backend.dns;

import no.nav.aura.basta.backend.dns.menandmice.MenAndMiceExecutor;
import no.nav.aura.basta.backend.vmware.orchestrator.WorkflowExecutor;
import no.nav.aura.basta.backend.vmware.orchestrator.request.DecomissionRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StartRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.request.StopRequest;
import no.nav.aura.basta.backend.vmware.orchestrator.response.OrchestratorResponse;
import no.nav.aura.basta.backend.vmware.orchestrator.response.Vm;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.util.Tuple;
import no.nav.aura.basta.util.XmlUtils;
import no.nav.generated.vmware.ws.WorkflowToken;
import no.nav.generated.vmware.ws.WorkflowTokenAttribute;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.List;

public class DnsService {

    private static final Logger logger = LoggerFactory.getLogger(DnsService.class);


    private MenAndMiceExecutor executor;

    public DnsService(MenAndMiceExecutor executor) {
        this.executor = executor;
    }

    public List<String> getUsers(){
        String session = executor.login();
        List<String> users = executor.getUsers(session);
        executor.logout(session);
        return users;
    }

    public static void main(String[] args) {

        DnsService dnsService = new DnsService(new MenAndMiceExecutor("http://10.83.3.45/_mmwebext/mmwebext.dll?Soap", "RA_S138206", "hiolr4tI01"));
        System.out.println(dnsService.getUsers());
    }

}
