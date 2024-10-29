package no.nav.aura.basta.rest.mq;

import org.hamcrest.Matchers;
import org.jboss.resteasy.spi.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThat;
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
        assertValidationFailsAndHasMessage("is too long");
    }

    @Test
    public void wrongFormatShouldNotValidate() {
        input.put("mqQueueName", "U1_APP_invalid");
        assertValidationFailsAndHasMessage("U1_APP_invalid");
    }

    @Test
    public void wrongNumberFormatShouldNotValidate() {
        input.put("maxMessageSize", "mange");
        assertValidationFailsAndHasMessage("regex");
    }
    
    @Test
    public void booleanFormatShouldFail() {
        input.put("createBackoutQueue", "x");
        assertValidationFailsAndHasMessage("not found in enum");
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
            assertThat(e.getMessage(), Matchers.containsString(message));
        }
    }

}
