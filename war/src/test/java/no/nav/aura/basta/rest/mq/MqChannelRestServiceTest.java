package no.nav.aura.basta.rest.mq;

import static org.junit.Assert.*;

import java.util.Collections;

import org.jboss.resteasy.spi.BadRequestException;
import org.junit.Test;

public class MqChannelRestServiceTest {

    @Test(expected=BadRequestException.class)
    public void emptyInputShouldNotValidate() {
        MqChannelRestService.validateInput(Collections.emptyMap());
    }

}
