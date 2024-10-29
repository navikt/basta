package no.nav.aura.basta.domain.result.vm;


import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class VMOrderResultTest {

    private VMOrderResult result;

     @BeforeAll
     public static void setFasitBaseUrl(){
         System.setProperty("fasit_rest_api_url", "http://e34apsl00136.devillo.no:8080/conf");
     }

     @BeforeEach
     public void setUp(){
        result = new VMOrderResult(new HashMap<>());

         result.addHostnameWithStatusAndNodeType("b27wasl00143.preprod.local", ResultStatus.ACTIVE, NodeType.JBOSS);
         result.addHostnameWithStatusAndNodeType("d26wasl00194.test.local", ResultStatus.STOPPED, NodeType.JBOSS);
         result.addHostnameWithStatusAndNodeType("d26wasl00195.devillo.no", ResultStatus.DECOMMISSIONED, null);
     }

    @Test
    public void testAsNodes() {
        Set<ResultDO> results = result.asResultDO();
        MatcherAssert.assertThat(results.size(), is(3));
    }

    @Test
    public void testAggregate() {
        MatcherAssert.assertThat(result.getDescription(), is(equalTo("MULTIPLE")));
    }
}