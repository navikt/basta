package no.nav.aura.basta.backend.mq;

import com.ibm.mq.constants.MQConstants;

/**
 * @author r137915
 * 
 */
public class MQAdminTest {

    public static void main(String[] a) throws Exception
    {
        String hostname = "e34apvl00007.devillo.no";
        int port = 1413;
        String mqManager = "MDLCLIENT05";
        String connectChannel = "SRVAURA.ADMIN";
        String adminUser = "srvAura";
        String adminPassword = "vAaGT0p1ee9o";
        String ipRange = "10.*";
        String username = "srvAura";
        int[] channelTypes = new int[] { MQConstants.MQCHT_RECEIVER, MQConstants.MQCHT_SENDER };
        String xmitQueueName = "SYSTEM.DEFAULT.XMITQ";
        String connectionName = "e34apvl00007.devillo.no";
        // Receiver: Req: Not start, just enable
        // Sender: Req: MQCACH_XMIT_Q_NAME
        MQQueue queue = new MQQueue("TEST_HP");

        try (MqAdmin mq = new MqAdmin(hostname, port, connectChannel, adminUser,adminPassword, mqManager)) {
            // Create queue and delete it
            mq.createOrUpdate(queue);
//            mq.setQueueAuthorization(queue);
            mq.print(queue);
            mq.delete(queue);

//            for (int channelType : channelTypes) {
//
//                MQChannel channel = new MQChannel("HP_TEST", channelType, xmitQueueName, connectionName);
//
//                // Create channel and set authorizations
//                mq.createOrUpdate(channel);
//                mq.setChannelAuthorization(channel, ipRange, username);
//
//                // Resetting channel sequence
//                mq.stopChannel(channel);
//                mq.resolveChannel(channel);
//                mq.resetChannelSequence(channel, 1);
//                mq.print(channel);
//
//                // Deleting channel
//                mq.delete(channel);
//                mq.deleteChannelAuthentication(channel, ipRange, username);
//            }

        }

    }

}
