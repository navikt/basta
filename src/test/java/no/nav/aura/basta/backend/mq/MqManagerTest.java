package no.nav.aura.basta.backend.mq;

import no.nav.aura.basta.domain.input.EnvironmentClass;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MqManagerTest {

    Map<EnvironmentClass, MqAdminUser> credentialMap = new HashMap<>();

    @Before
    public void setup(){
        credentialMap.put(EnvironmentClass.u, new MqAdminUser("user", "password", "channel"));
    }
    
    

    @Test
    public void createQueueManangerFromValidUri() {
        MqQueueManager queueManager = new MqQueueManager(URI.create("mq://e26apvl100.test.local:1411/MDLCLIENT03"),
                EnvironmentClass.u, credentialMap);
        assertEquals("e26apvl100.test.local", queueManager.getHost());
        assertEquals(1411, queueManager.getPort());
        assertEquals("MDLCLIENT03", queueManager.getMqManagerName());
    }
    
}
