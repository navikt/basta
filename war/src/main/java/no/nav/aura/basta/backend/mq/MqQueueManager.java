package no.nav.aura.basta.backend.mq;

import java.io.IOException;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFMessageAgent;

public class MqQueueManager{

    private PCFMessageAgent agent;
    private Logger log = LoggerFactory.getLogger(MqQueueManager.class);
    private String mqManagerName;
    private String host;
    private int port;

    public MqQueueManager(String host, int port, String mqManagerName) {

        this.host = host;
        this.port = port;
        this.mqManagerName = mqManagerName;
        
    }
    
    public void connect( MqAdminUser adminUser){
        try {
            // System.setProperty("javax.net.ssl.trustStore", "truststore.jts");
            // System.setProperty("javax.net.ssl.trustStorePassword", "cliTrustStore");
            // System.setProperty("javax.net.ssl.keyStore", "keystore.jks");
            // System.setProperty("javax.net.ssl.keyStorePassword", "");

            log.info("Connecting to {}with user {}", toString(), adminUser);

            Hashtable<Object, Object> properties = new Hashtable<>();
            properties.put(MQConstants.HOST_NAME_PROPERTY, host);
            properties.put(MQConstants.CHANNEL_PROPERTY, adminUser.getChannelName());
            properties.put(MQConstants.PORT_PROPERTY, port);
            properties.put(MQConstants.USER_ID_PROPERTY, adminUser.getUsername());
            properties.put(MQConstants.PASSWORD_PROPERTY, adminUser.getPassword());
            properties.put(MQConstants.TRANSPORT_PROPERTY, MQConstants.TRANSPORT_MQSERIES);
            //Timeout?

            // properties.put(MQConstants.SSL_CIPHER_SUITE_PROPERTY, "SSL_RSA_WITH_RC4_128_MD5");
            // properties.put(MQConstants.SSL_CERT_STORE_PROPERTY, System.getProperty("javax.net.ssl.trustStore"));

            MQQueueManager mqQueueManager = new MQQueueManager(mqManagerName, properties);
            agent = new PCFMessageAgent(mqQueueManager);
            agent.setCheckResponses(true);
        } catch (MQException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    public PCFMessage[] execute(PCFMessage request) {
        try {
            return agent.send(request);
        } catch (MQException | IOException e) {
            throw new RuntimeException(e);
        }

    }
    
    @Override
    public String toString() {
        return String.format("MQQueueManager %s@%s:%s ",this.mqManagerName, this.host, this.port);
    }

    public void close() {
        try {
            log.debug("Closing connection to {}", this );
            agent.disconnect();
        } catch (MQException e) {
            throw new RuntimeException(e);
        }

    }
}
