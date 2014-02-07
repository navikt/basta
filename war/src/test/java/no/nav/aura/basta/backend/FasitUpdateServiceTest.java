package no.nav.aura.basta.backend;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.net.URL;

import javax.inject.Inject;

import no.nav.aura.basta.persistence.Node;
import no.nav.aura.basta.persistence.NodeRepository;
import no.nav.aura.basta.persistence.NodeType;
import no.nav.aura.basta.persistence.Order;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.vmware.orchestrator.request.Vm.MiddleWareType;
import no.nav.aura.envconfig.client.FasitRestClient;

import org.jboss.resteasy.spi.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
@TransactionConfiguration
@Transactional
public class FasitUpdateServiceTest {

    @Inject
    private NodeRepository nodeRepository;

    @Inject
    private FasitUpdateService fasitUpdateService;

    @Inject
    private FasitRestClient fasitRestClient;

    @Test
    public void removeFasitEntity() throws Exception {
        createHost("hostindb", null);
        Node hostInFasit = createHost("hostinfasit", new URL("http://delete.me"));
        doNothing().when(fasitRestClient).delete(Mockito.eq(hostInFasit.getFasitUrl().toURI()), Mockito.anyString());
        Node removedHost = createHost("removedhost", new URL("http://crash.on.me"));
        doThrow(NotFoundException.class).when(fasitRestClient).delete(Mockito.eq(removedHost.getFasitUrl().toURI()), Mockito.anyString());

        fasitUpdateService.removeFasitEntity(new Order(NodeType.DECOMMISSIONING), ", hostindb, removedhost, hostinfasit, dunnohost,  ");

        verify(fasitRestClient).delete(Mockito.eq(hostInFasit.getFasitUrl().toURI()), Mockito.anyString());
        verify(fasitRestClient).delete(Mockito.eq(removedHost.getFasitUrl().toURI()), Mockito.anyString());
    }

    private Node createHost(String hostname, URL fasitUrl) {
        Node hostInFasit = new Node(null, hostname, null, 1, 1024, null, MiddleWareType.jb, null);
        hostInFasit.setFasitUrl(fasitUrl);
        nodeRepository.save(hostInFasit);
        return hostInFasit;
    }

}
