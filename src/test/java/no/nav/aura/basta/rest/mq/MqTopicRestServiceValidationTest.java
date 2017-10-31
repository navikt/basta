package no.nav.aura.basta.rest.mq;

import static no.nav.aura.basta.rest.mq.MqTopicRestService.validateInput;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.jboss.resteasy.spi.BadRequestException;
import org.junit.Before;
import org.junit.Test;

public class MqTopicRestServiceValidationTest {

    private Map<String, String> input;

    @Before
    public void setup() {
        input = new HashMap<>();
        input.put("environmentClass", "u");
        input.put("environmentName", "u1");
        input.put("application", "myapp");
        input.put("fasitAlias", "myapp_topic");
        input.put("queueManager", "mq://host:123/mqGateway");
        input.put("description", "bla bla bla");
        input.put("topicString", "u1/my/topic/string");
       

    }

    @Test(expected = BadRequestException.class)
    public void emptyInputShouldNotValidate() {
        validateInput(Collections.emptyMap());
    }

    @Test
    public void toLongShouldNotValidate() {
        input.put("description", "bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla ");
        assertValidationFailsAndHasMessage("is too long");
    }

    @Test
    public void wrongFormatShouldNotValidate() {
        input.put("topicString", "app/some /space ting");
        assertValidationFailsAndHasMessage("app/some /space ting");
    }

    
    @Test
    public void descriptionWithSpecialCharsShouldFail() {
        input.put("description", "halla balla @!æøå*");
        assertValidationFailsAndHasMessage("regex");
    }


    @Test
    public void shouldValidate() {
        validateInput(input);
    }

    private void assertValidationFailsAndHasMessage(String message) {
        try {
            validateInput(input);
            fail("Validation did not fail");
        } catch (BadRequestException e) {
            assertThat(e.getMessage(), Matchers.containsString(message));
        }
    }

}
