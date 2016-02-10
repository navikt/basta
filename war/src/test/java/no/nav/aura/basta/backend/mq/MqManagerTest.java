package no.nav.aura.basta.backend.mq;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import no.nav.aura.basta.domain.input.EnvironmentClass;

public class MqManagerTest {
    
    @Before
    public void setup(){
        System.setProperty("mqadmin.u.username", "user");
        System.setProperty("mqadmin.u.password", "password");
        System.setProperty("mqadmin.u.channel", "channel");
    }
    
    

    @Test
    public void createQueueManangerFromValidUri() {
        MqQueueManager queueManager = new MqQueueManager(URI.create("mq://e26apvl100.test.local:1411/MDLCLIENT03"), EnvironmentClass.u);
        assertEquals("e26apvl100.test.local", queueManager.getHost());
        assertEquals(1411, queueManager.getPort());
        assertEquals("MDLCLIENT03", queueManager.getMqManagerName());
    }
    
}
