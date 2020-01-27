package no.nav.aura.basta.domain.input.mq;

import no.nav.aura.basta.security.User;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class MqOrderInputTest {

    private MqOrderInput input;

    @Before
    public void setup() {
        input = new MqOrderInput(new HashMap<>(), MQObjectType.Queue);
        input.setEnvironment("myEnv");
        input.setApplication("myapp");
    }

    @Test
    public void testDefaultDescriptionGeneration() {
        assertEquals("Queue for myapp in myEnv. Created by a123123123 (My Name)", input.generateDescription(new User("a123123123", "My Name", new HashSet<>(), false)));
        assertThat("norske bokstaver",input.generateDescription(new User("a123123123", "Bæ bø", new HashSet<>(), false)), not(containsString("bø")));
        assertEquals("lange navn",64, input.generateDescription(new User("a123123123", "veldig langt navn 12323123123123123123123123123123909090909", new HashSet<>(), false)).length());
    }
}
