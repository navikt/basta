package no.nav.aura.basta.domain.input.mq;

import no.nav.aura.basta.security.User;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MqOrderInputTest {

    private MqOrderInput input;

    @BeforeEach
    public void setup() {
        input = new MqOrderInput(new HashMap<>(), MQObjectType.Queue);
        input.setEnvironment("myEnv");
        input.setApplication("myapp");
    }

    @Test
    public void testDefaultDescriptionGeneration() {
        assertEquals("Queue for myapp in myEnv. Created by a123123123 (My Name)", input.generateDescription(new User("a123123123", "My Name", new HashSet<>(), false)));
        assertThat("norske bokstaver", input.generateDescription(new User("a123123123", "Bæ bø", new HashSet<>(), false)), not(containsString("bø")));
        assertEquals(64, input.generateDescription(new User("a123123123", "veldig langt navn 12323123123123123123123123123123909090909", new HashSet<>(), false)).length());
    }
}
