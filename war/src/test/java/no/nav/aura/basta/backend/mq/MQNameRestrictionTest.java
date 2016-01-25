package no.nav.aura.basta.backend.mq;

import static org.junit.Assert.*;

import org.junit.Test;

public class MQNameRestrictionTest {

    @Test
    public void testChannelEnvNameConventions() {
        assertEquals("U1_MYAPP", new MqChannel("u1", "myApp").getName());
        assertEquals("TPRU1_MYAPP", new MqChannel("tpr-u1", "myApp").getName());
        assertEquals("STJU1_MYAPP", new MqChannel("stjerneeksempel-u1", "myApp").getName());
        assertEquals("HEIU1_MYAPP", new MqChannel("h_e_i_hallo-hadet-u1", "myApp").getName());
        assertEquals("DTEST_MYAPP", new MqChannel("detteermin-test", "myApp").getName());
    }

    @Test
    public void testChannelAppNameConventions() {
        assertEquals("U1_MYAPPCORE", new MqChannel("u1", "myApp_core").getName());
        assertEquals("U1_MYAPP123456789", new MqChannel("u1", "myApp_123456789123456789").getName());
    }

    @Test
    public void testChannelMaxLength() {
        assertEquals(20, new MqChannel("tullogt�yslsjdfljdlf-u1", "myApp_cl�sjkdf�sdjkf-klasdjflas_kdhsfk.").getName().length());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalenvironmentName() {
        new MqChannel("stupid-illegalenv", "app1");
    }

//    @Test
//    public void testQueueName() {
//        assertEquals("U1_MYAPP_MYQUEUE", new MQQueue("myqueue", "u1", "myApp").getName());
//        assertEquals("U1_MYAPP_MYQUEUE12345678901234567", new MQQueue("myqueue12345678901234567890", "u1", "myApp").getName());
//    }
//
//    @Test
//    public void testQueueNameChannelMaxLength() {
//        assertEquals(45, new MQQueue("myqueue12345678901234567890_123123123123123123123", "lsdjlsdjflsdjflsdjf-u1", "myAppe1028301283012830123801283-081-20_3812038").getName().length());
//    }

}
