package no.nav.aura.basta.backend.mq;

import com.ibm.mq.constants.MQConstants;

/**
 * @author r137915
 * 
 */
public class MQAdminTestMain {

    public static void main(String[] a) throws Exception {
        String hostname = "e34apvl00007.devillo.no";
        int port = 1413;
        String mqManager = "MDLCLIENT05";
        String connectChannel = "SRVAURA.ADMIN";
        String adminUser = "srvAura";
        String adminPassword = "vAaGT0p1ee9o";
        String ipRange = "*";
        String username = "srvphoton_u";
        String password= "1iGbynpgoQ";
//        int[] channelTypes = new int[] { MQConstants.MQCHT_RECEIVER, MQConstants.MQCHT_SENDER };
//        String xmitQueueName = "SYSTEM.DEFAULT.XMITQ";
//        String connectionName = "e34apvl00007.devillo.no";
        // Receiver: Req: Not start, just enable
        // Sender: Req: MQCACH_XMIT_Q_NAME
        MqQueue queue = new MqQueue("TEST_HP", 1, 100, "Test aura");
        
        MqAdminUser mqAdminUser = new MqAdminUser(adminUser, adminPassword, connectChannel); 
        MqQueueManager queueManager=new MqQueueManager(hostname, port,  mqManager);

        try (MqService mq = new MqService(queueManager, mqAdminUser)) {
//            if(mq.exists(queue)){
//                mq.delete(queue);
//            }
//            mq.create(queue);
//            // mq.setQueueAuthorization(queue);
//            mq.print(queue);

            
             MqChannel channel = new MqChannel("ZZ_TEST_CHANNEL");
            
             // Create channel and set authorizations
             if(mq.exists(channel)){
                 mq.delete(channel);
                 mq.deleteChannelAuthentication(channel, ipRange, username);
             }
             mq.create(channel);
             mq.setChannelAuthorization(channel, ipRange, username);
            
//             // Resetting channel sequence
//             mq.stopChannel(channel);
//             mq.resolveChannel(channel);
//             mq.resetChannelSequence(channel, 1);
             mq.print(channel);
             

        }
        
        MqAdminUser testUser = new MqAdminUser(username, password, "ZZ_TEST_CHANNEL");
        queueManager.connect(testUser);
        queueManager.close();

    }

}
