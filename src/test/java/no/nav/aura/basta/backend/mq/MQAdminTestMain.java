package no.nav.aura.basta.backend.mq;

import no.nav.aura.basta.domain.input.EnvironmentClass;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author r137915
 * 
 */
public class MQAdminTestMain {

    public static void main(String[] a) throws Exception {
        // String hostname = "e34apvl00007.devillo.no";
        // int port = 1413;
        // String mqManager = "MDLCLIENT05";
        // URI mqUrl=URI.create("mq://e34apvl00007.devillo.no:1413/MDLCLIENT05");
        URI mqUrl = URI.create("mq://e26apvl100.test.local:1411/MDLCLIENT03");
        Map<String, String> credMap = new HashMap<>();
        String connectChannel = "SRVAURA.ADMIN";
        String adminUser = "srvAura";
        String adminPassword = "vAaGT0p1ee9o";
        Map<EnvironmentClass, MqAdminUser> envCredentials = new HashMap<>();
        envCredentials.put(EnvironmentClass.u, new MqAdminUser(adminUser, adminPassword, connectChannel));

        String ipRange = "*";
        String username = "srvappserver";
        String password = "1iGbynpgoQ";
        // int[] channelTypes = new int[] { MQConstants.MQCHT_RECEIVER, MQConstants.MQCHT_SENDER };
        // String xmitQueueName = "SYSTEM.DEFAULT.XMITQ";
        // String connectionName = "e34apvl00007.devillo.no";
        // Receiver: Req: Not start, just enable
        // Sender: Req: MQCACH_XMIT_Q_NAME
        MqQueue queue = new MqQueue("TEST_HP", 1, 100, "Test aura");
        MqTopic topic = new MqTopic("test_topic", "u1/test/slett/meg");

        MqAdminUser mqAdminUser = new MqAdminUser(adminUser, adminPassword, connectChannel);
        MqQueueManager queueManager = new MqQueueManager(mqUrl, mqAdminUser);
        MqService mq = new MqService(envCredentials);
        // if (mq.queueExists(queueManager, queue.getName())) {
        // mq.deleteQueue(queueManager, queue);
        // }
        //
        // mq.createQueue(queueManager, queue);
        // mq.print(queueManager, queue.getName());
        //
        MqChannel channel = new MqChannel("ZZ_TEST_CHANNEL");
        channel.setUserName(username);
        channel.setDescription("test");

        System.out.println(mq.findChannelNames(queueManager, "U1_*"));
        // Create channel and set authorizations
//        if (mq.channelExists(queueManager, channel)) {
//            mq.deleteChannel(queueManager, channel);
//        }
//        mq.createChannel(queueManager, channel);
        // System.out.println(mq.getQueue(queueManager, "U2_CAMELTOES_SLETT_MEG2_XX"));
        // System.out.println(mq.getQueue(queueManager, "QA.POC10_WASDEPLOY_TEST_WASDEPLOYTEST_SLETTMEG"));
        // System.out.println(mq.getQueueStatus(queueManager, "CD_U1_AUTODEPLOY_TEST_SLETTMEG"));
        // System.out.println(mq.findQueuesAliases(queueManager, "*"));

        // mq.disableQueue(queueManager, queue);
        // mq.enableQueue(queueManager, queue);
//        System.out.println(mq.getTopics(queueManager));
        // mq.enableTopic(queueManager, topic);
    }

}
