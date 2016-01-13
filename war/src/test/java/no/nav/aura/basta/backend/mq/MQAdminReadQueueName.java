package no.nav.aura.basta.backend.mq;

import java.util.Hashtable;

import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.CMQC;
import com.ibm.mq.pcf.CMQCFC;
import com.ibm.mq.pcf.MQCFH;
import com.ibm.mq.pcf.MQCFIN;
import com.ibm.mq.pcf.MQCFSL;
import com.ibm.mq.pcf.MQCFST;
import com.ibm.mq.pcf.PCFAgent;
import com.ibm.mq.pcf.PCFParameter;

public class MQAdminReadQueueName {

    public static void main(String[] a) throws Exception
    {
        String hostname = "e26apvl100.test.local";
        int port = 1411;
        String channel = "MOD.MDLCLIENT03";
        String userid = "modadmin";

        // Connect a PCFAgent to the specified queue manager
        System.out.print("Connecting to queue manager at " +
                hostname + ":" + port + " over channel " + channel + "... ");
        Hashtable<Object, Object> properties = new Hashtable<>();
        properties.put(MQConstants.HOST_NAME_PROPERTY, hostname);
        properties.put(MQConstants.PORT_PROPERTY, port);
        properties.put(MQConstants.CHANNEL_PROPERTY, channel);
        properties.put(MQConstants.USER_ID_PROPERTY, userid);
        MQQueueManager mqQueueManager = new MQQueueManager("MDLCLIENT03", properties);
        PCFAgent agent = new PCFAgent(mqQueueManager);

        System.out.println("Connected.");

        // Use the agent to send the request

        System.out.print("Sending PCF request... ");
        PCFParameter[] parameters =
        {
                new MQCFST(CMQC.MQCA_Q_NAME, "TEST*"),
                new MQCFIN(CMQC.MQIA_Q_TYPE, CMQC.MQQT_ALL)
        };
        MQMessage[] responses = agent.send(CMQCFC.MQCMD_INQUIRE_Q_NAMES, parameters);
        System.out.println("Received reply.");
        MQCFH cfh = new MQCFH(responses[0]);

        // Check the PCF header (MQCFH) in the first response message

        if (cfh.reason == 0)
        {
            System.out.println("Queue names:");
            MQCFSL cfsl = new MQCFSL(responses[0]);

            for (int i = 0; i < cfsl.strings.length; i++)
            {
                System.out.println("\t" + cfsl.strings[i]);
            }
        }
        else
        {
            System.out.println(cfh);

            // Walk through the returned parameters describing the error

            for (int i = 0; i < cfh.parameterCount; i++)
            {
                System.out.println(PCFParameter.nextParameter(responses[0]));
            }
        }

        // Disconnect

        System.out.print("Disconnecting... ");
        agent.disconnect();
        System.out.println("Done.");
    }
}
