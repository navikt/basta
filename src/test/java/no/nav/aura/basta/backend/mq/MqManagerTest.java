package no.nav.aura.basta.backend.mq;

import no.nav.aura.basta.domain.input.EnvironmentClass;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class MqManagerTest {
    
    @Before
    public void setup(){
        System.setProperty("BASTA_MQ_U_USERNAME", "user");
        System.setProperty("BASTA_MQ_U_PASSWORD", "password");
        System.setProperty("BASTA_MQ_U_CHANNEL", "channel");
    }
    
    

    @Test
    public void createQueueManangerFromValidUri() {
        MqQueueManager queueManager = new MqQueueManager(URI.create("mq://e26apvl100.test.local:1411/MDLCLIENT03"), EnvironmentClass.u);
        assertEquals("e26apvl100.test.local", queueManager.getHost());
        assertEquals(1411, queueManager.getPort());
        assertEquals("MDLCLIENT03", queueManager.getMqManagerName());
    }
    
}
