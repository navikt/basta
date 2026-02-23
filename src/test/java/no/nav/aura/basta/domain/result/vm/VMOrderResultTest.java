package no.nav.aura.basta.domain.result.vm;


import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.basta.util.FasitHelper;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FasitHelper.class})
public class VMOrderResultTest {
    
    private VMOrderResult result;

    @BeforeAll
    public static void setFasitBaseUrl(){
        System.setProperty("fasit_base_url", "http://fasiturl.com");
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