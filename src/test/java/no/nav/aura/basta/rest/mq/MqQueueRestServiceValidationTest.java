package no.nav.aura.basta.rest.mq;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.BadRequestException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MqQueueRestServiceValidationTest {

    private Map<String, String> input;

    @BeforeEach
    public void setup() {
        input = new HashMap<>();
        input.put("environmentClass", "u");
        input.put("environmentName", "u1");
        input.put("application", "myapp");
        input.put("fasitAlias", "myapp_channel");
        input.put("queueManager", "mq://host:123/mqGateway");
        input.put("description", "bla bla bla");
        input.put("mqQueueName", "U1_APP_SOMENAME");
        input.put("maxMessageSize", "1");
        input.put("queueDepth", "1000");
        input.put("createBackoutQueue", "false");

    }

    @Test
    public void emptyInputShouldNotValidate() {
        assertThrows(BadRequestException.class, () -> MqQueueRestService.validateInput(Collections.emptyMap()));
    }

    @Test
    public void toLongShouldNotValidate() {
        input.put("mqQueueName", "U1_APP_KOEN12345678901234567890123123123123123123123123123123123123123123123123123123123123123123123123");
        assertValidationFailsAndHasMessage("must be at most");
    }

    @Test
    public void wrongFormatShouldNotValidate() {
        input.put("mqQueueName", "U1_APP_invalid");
        assertValidationFailsAndHasMessage("mqQueueName: does not match the regex pattern ^[A-Z0-9._]*$");
    }

    @Test
    public void wrongNumberFormatShouldNotValidate() {
        input.put("maxMessageSize", "mange");
        assertValidationFailsAndHasMessage("regex");
    }
    
    @Test
    public void booleanFormatShouldFail() {
        input.put("createBackoutQueue", "x");
        assertValidationFailsAndHasMessage("does not have a value in the enumeration [\"true\", \"false\"]");
    }
    
    @Test
    public void descriptionShouldbeValid() {
        input.put("description", "halla balla");
    }
    
    @Test
    public void descriptionWithSpecialCharsShouldFail() {
        input.put("description", "halla balla @!æøå*");
        assertValidationFailsAndHasMessage("regex");
    }



    @Test
    public void shouldValidate() {
        MqQueueRestService.validateInput(input);
    }

    private void assertValidationFailsAndHasMessage(String message) {
        try {
            MqQueueRestService.validateInput(input);
            fail("Validation did not fail");
        } catch (BadRequestException e) {
            MatcherAssert.assertThat(e.getMessage(), Matchers.containsString(message));
        }
    }

}
