package no.nav.aura.basta.rest.mq;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.jboss.resteasy.spi.BadRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class MqChannelRestValidationServiceTest {
    
    private Map<String, String> input;

    @BeforeEach
    public void setup(){
        input = new HashMap<>();
        input.put("environmentClass", "u");
        input.put("environmentName", "u1");
        input.put("application", "myapp");
        input.put("fasitAlias", "myapp_channel");
        input.put("queueManager", "mqGateway");
        input.put("username", "mqUser");
        input.put("mqChannelName", "U1_APP_KOEN");
        input.put("description", "blabla bla");
    }
    

    @Test
    public void emptyInputShouldNotValidate() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            MqChannelRestService.validateInput(Collections.emptyMap());
        });
    }
    
    @Test
    public void toLongShouldNotValidate() {
        input.put("mqChannelName", "U1_APP_KOEN12345678901234567890");
        assertValidationFailsAndHasMessage("is too long");
    }
    
    @Test
    public void wrongFormatShouldNotValidate() {
        input.put("mqChannelName", "U1_APP_invalid");
        assertValidationFailsAndHasMessage("U1_APP_invalid");
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
            assertThat(e.getMessage(), Matchers.containsString(message));
        }
    }

}
