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

public class MqChannelRestValidationServiceTest {
    
    private Map<String, String> input;

    @BeforeEach
    public void setup(){
        input = new HashMap<>();
        input.put("environmentClass", "u");
        input.put("environmentName", "u1");
        input.put("application", "myapp");
        input.put("fasitAlias", "myapp_channel");
        input.put("queueManager", "mq://hostname:1414/mqGateway");
        input.put("username", "mqUser");
        input.put("mqChannelName", "U1_APP_KOEN");
        input.put("description", "blabla bla");
    }
    

    @Test
    public void emptyInputShouldNotValidate() {
        assertThrows(BadRequestException.class, () -> MqChannelRestService.validateInput(Collections.emptyMap()));
    }
    
    @Test
    public void toLongShouldNotValidate() {
        input.put("mqChannelName", "U1_APP_KOEN12345678901234567890");
        assertValidationFailsAndHasMessage("must be at most");
    }
    
    @Test
    public void wrongFormatShouldNotValidate() {
        input.put("mqChannelName", "U1_APP_invalid");
        assertValidationFailsAndHasMessage("mqChannelName: does not match the regex pattern ^[A-Z0-9._]*$");
    }
    
    @Test
    public void shouldValidate() {
        MqChannelRestService.validateInput(input);
    }
    
    private void assertValidationFailsAndHasMessage(String message) {
        try {
            MqChannelRestService.validateInput(input);
            fail("Validation did not fail");
        } catch (BadRequestException e) {
        	MatcherAssert.assertThat(e.getMessage(), Matchers.containsString(message));
        }
    }

}
