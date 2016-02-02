package no.nav.aura.basta.rest.mq;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.resteasy.spi.BadRequestException;
import org.junit.Before;
import org.junit.Test;

public class MqChannelRestServiceTest {
    
    private Map<String, String> input;

    @Before
    public void setup(){
        input = new HashMap<>();
        input.put("environmentClass", "u");
        input.put("environmentName", "u1");
        input.put("application", "myapp");
        input.put("fasitAlias", "myapp_channel");
        input.put("queueManager", "mqGateway");
        input.put("username", "mqUser");
        input.put("mqChannelName", "U1_APP_KOEN");
    }
    

    @Test(expected=BadRequestException.class)
    public void emptyInputShouldNotValidate() {
        MqChannelRestService.validateInput(Collections.emptyMap());
    }
    
    @Test(expected=BadRequestException.class)
    public void toLongShouldNotValidate() {
        input.put("mqChannelName", "U1_APP_KOEN12345678901234567890");
        MqChannelRestService.validateInput(input);
    }
    
    @Test(expected=BadRequestException.class)
    public void wrongFormatShouldNotValidate() {
        input.put("mqChannelName", "U1_APP_invalid");
        MqChannelRestService.validateInput(input);
    }
    
    @Test
    public void shouldValidate() {
        MqChannelRestService.validateInput(input);
    }

}
