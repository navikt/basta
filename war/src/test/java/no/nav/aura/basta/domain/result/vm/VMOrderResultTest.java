package no.nav.aura.basta.domain.result.vm;


import com.google.common.collect.Maps;
import com.sun.org.apache.xpath.internal.patterns.NodeTestFilter;
import no.nav.aura.basta.domain.input.vm.NodeStatus;
import no.nav.aura.basta.domain.input.vm.NodeType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;
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

        result.addHostnameWithStatusAndNodeType("b27wasl00143.preprod.local", NodeStatus.ACTIVE);
        result.addHostnameWithStatusAndNodeType("d26wasl00194.test.local", NodeStatus.STOPPED);
        result.addHostnameWithStatusAndNodeType("d26wasl00195.devillo.no", NodeStatus.DECOMMISSIONED);

        Set<VMNode> nodes = result.asNodes();
        assertThat(nodes.size(), is(3));
        for (VMNode node : nodes) {
            System.out.println(node.getHostname() + "  " + node.getStatus() + "  " + node.getFasitUrl());
        }

        for (Map.Entry<String, String> entry : result.copy().entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }


    }


}