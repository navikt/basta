package no.nav.aura.basta.domain.input.mq;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.HashSet;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import no.nav.aura.basta.security.User;

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
//        System.out.println(input.getDefaultQueueDescription(new User("a123123123", "My Name", new HashSet<>(), false)));
        assertEquals("Queue for myapp in myEnv. Created by a123123123 (My Name)", input.generateDescription(new User("a123123123", "My Name", new HashSet<>(), false)));
        assertThat("norske bokstaver",input.generateDescription(new User("a123123123", "Bæ bø", new HashSet<>(), false)), not(containsString("bø")));
        assertEquals("lange navn",64, input.generateDescription(new User("a123123123", "veldig langt navn 12323123123123123123123123123123909090909", new HashSet<>(), false)).length());
    }
    
    @Test
    public void testTopicNameGeneration() {
//        System.out.println(input.getDefaultQueueDescription(new User("a123123123", "My Name", new HashSet<>(), false)));
        assertEquals("MYENV_MYAPP_BAZ.BAR.FOO", input.generateTopicName("foo/bar/baz"));
        assertThat("max langde 48 tegn", input.generateTopicName("foo/bar/baz1234567890123123123123/21123123/12323423423/123123123123/1241231231231nljljljn").length(), Matchers.lessThanOrEqualTo(48));
        assertEquals("MYENV_MYAPP_BAZ.BAR.FOO", input.generateTopicName("myenv/foo/bar/baz"));
        assertEquals("MYENV_MYAPP_BAZDAZ.BAR.FOO", input.generateTopicName("myenv/foo/bar/baz-daz"));
    }
   
}
