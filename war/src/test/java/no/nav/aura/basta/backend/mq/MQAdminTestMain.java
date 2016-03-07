package no.nav.aura.basta.backend.mq;

import java.net.URI;

import com.ibm.mq.constants.MQConstants;

/**
 * @author r137915
 * 
 */
public class MQAdminTestMain {

    public static void main(String[] a) throws Exception {
//        String hostname = "e34apvl00007.devillo.no";
//        int port = 1413;
//        String mqManager = "MDLCLIENT05";
        URI mqUrl=URI.create("mq://e34apvl00007.devillo.no:1413/MDLCLIENT05");
//       URI mqUrl=URI.create("mq://e26apvl100.test.local:1411/MDLCLIENT03");
        
        String connectChannel = "SRVAURA.ADMIN";
        String adminUser = "srvAura";
        String adminPassword = "vAaGT0p1ee9o";
        String ipRange = "*";
        String username = "srvphoton_u";
        String password = "1iGbynpgoQ";
        // int[] channelTypes = new int[] { MQConstants.MQCHT_RECEIVER, MQConstants.MQCHT_SENDER };
        // String xmitQueueName = "SYSTEM.DEFAULT.XMITQ";
        // String connectionName = "e34apvl00007.devillo.no";
        // Receiver: Req: Not start, just enable
        // Sender: Req: MQCACH_XMIT_Q_NAME
        MqQueue queue = new MqQueue("TEST_HP", 1, 100, "Test aura");

        MqAdminUser mqAdminUser = new MqAdminUser(adminUser, adminPassword, connectChannel);
        MqQueueManager queueManager = new MqQueueManager(mqUrl, mqAdminUser);
        MqService mq = new MqService();
//        if (mq.queueExists(queueManager, queue.getName())) {
//            mq.deleteQueue(queueManager, queue);
//        }
//
//        mq.createQueue(queueManager, queue);
//        mq.print(queueManager, queue.getName());
//
//         MqChannel channel = new MqChannel("ZZ_TEST_CHANNEL", username, "testkanal");
//        
//         // Create channel and set authorizations
//         if (mq.exists(queueManager, channel)) {
//         mq.delete(queueManager, channel);
//         mq.deleteChannelAuthentication(queueManager, channel, ipRange, username);
//         }
//         mq.create(queueManager, channel);
//         mq.setChannelAuthorization(queueManager, channel);

        System.out.println(mq.getQueueStatus(queueManager, "CD_U1_AUTODEPLOY_TEST_SLETTMEG").getCurrentQueueDepth());
         System.out.println(mq.findQueues(queueManager, "*"));
    }

}
