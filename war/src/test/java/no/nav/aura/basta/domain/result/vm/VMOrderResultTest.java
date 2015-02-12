package no.nav.aura.basta.domain.result.vm;


import com.google.common.collect.Maps;
import no.nav.aura.basta.domain.input.vm.ResultStatus;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class VMOrderResultTest {

    private VMOrderResult result;

     @BeforeClass
     public static void setFasitBaseUrl(){
         System.setProperty("fasit.rest.api.url", "http://e34apsl00136.devillo.no:8080/conf");
     }

     @Before
     public void setUp(){
         result = new VMOrderResult(Maps.newHashMap());
     }

    @Test
    public void testAsNodes() throws Exception {

        result.addHostnameWithStatusAndNodeType("b27wasl00143.preprod.local", ResultStatus.ACTIVE);
        result.addHostnameWithStatusAndNodeType("d26wasl00194.test.local", ResultStatus.STOPPED);
        result.addHostnameWithStatusAndNodeType("d26wasl00195.devillo.no", ResultStatus.DECOMMISSIONED);

        Set<ResultDO> results = result.asResultDO();
        assertThat(results.size(), is(3));


    }


}