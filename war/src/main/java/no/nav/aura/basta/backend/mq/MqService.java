package no.nav.aura.basta.backend.mq;

import java.util.Enumeration;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFParameter;

public class MqService implements AutoCloseable {

    private MqQueueManager queueManager;
    private Logger log = LoggerFactory.getLogger(MqService.class);

    public MqService(MqQueueManager queueManager, MqAdminUser adminUser) {
        // sjekk at du får koblet til
        queueManager.connect(adminUser);
    }
    
    

    public void createOrUpdate(MqQueue queue) {
        if (!exists(queue)) {
            PCFMessage createQueuerequest = new PCFMessage(MQConstants.MQCMD_CREATE_Q);
            createQueuerequest.addParameter(MQConstants.MQCA_Q_NAME, queue.getName());
            createQueuerequest.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_LOCAL);
            createQueuerequest.addParameter(MQConstants.MQCA_Q_DESC, queue.getDescription());

            execute(createQueuerequest);
            log.info("Created queue {}", queue.getName());

            PCFMessage createAliasrequest = new PCFMessage(MQConstants.MQCMD_CREATE_Q);
            createAliasrequest.addParameter(MQConstants.MQCA_Q_NAME, queue.getAlias());
            createAliasrequest.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_ALIAS);
            createAliasrequest.addParameter(MQConstants.MQCA_BASE_OBJECT_NAME, queue.getName());

            execute(createAliasrequest);
            log.info("Created queue alias: QA." + queue.getName());

            PCFMessage getQueueAuthrequest = new PCFMessage(MQConstants.MQCMD_INQUIRE_AUTH_RECS);
            getQueueAuthrequest.addParameter(MQConstants.MQIACF_AUTH_OPTIONS, MQConstants.MQAUTHOPT_NAME_ALL_MATCHING + MQConstants.MQAUTHOPT_ENTITY_EXPLICIT);
            getQueueAuthrequest.addParameter(MQConstants.MQCACF_AUTH_PROFILE_NAME, "QA." + queue.getName());
            getQueueAuthrequest.addParameter(MQConstants.MQIACF_OBJECT_TYPE, MQConstants.MQOT_Q);

            execute(getQueueAuthrequest);
        } else {
            PCFMessage updateRequest = new PCFMessage(MQConstants.MQCMD_CHANGE_Q);
            updateRequest.addParameter(MQConstants.MQCA_Q_NAME, queue.getName());
            updateRequest.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_LOCAL);
            updateRequest.addParameter(MQConstants.MQCA_Q_DESC, queue.getDescription() + " updated");

            execute(updateRequest);
            log.info("Queue {} exists, updating ", queue.getName());
        }
    }

    public void setQueueAuthorization(MqQueue queue) {
        int[] listRemoveQueueAuth = new int[1];
        listRemoveQueueAuth[0] = MQConstants.MQAUTH_ALL;

        int[] listAddQueueAuth = new int[5];
        listAddQueueAuth[0] = MQConstants.MQAUTH_BROWSE;
        listAddQueueAuth[1] = MQConstants.MQAUTH_DISPLAY;
        listAddQueueAuth[2] = MQConstants.MQAUTH_INQUIRE;
        listAddQueueAuth[3] = MQConstants.MQAUTH_INPUT;
        listAddQueueAuth[4] = MQConstants.MQAUTH_OUTPUT;

        PCFMessage deleteQueueAuthrequest = new PCFMessage(MQConstants.MQCMD_SET_AUTH_REC);
        deleteQueueAuthrequest.addParameter(MQConstants.MQCACF_AUTH_PROFILE_NAME, queue.getAlias());
        deleteQueueAuthrequest.addParameter(MQConstants.MQIACF_OBJECT_TYPE, MQConstants.MQOT_Q);
        deleteQueueAuthrequest.addParameter(MQConstants.MQIACF_AUTH_REMOVE_AUTHS, listRemoveQueueAuth);
        deleteQueueAuthrequest.addParameter(MQConstants.MQCACF_GROUP_ENTITY_NAMES, getGroupList("mqusers"));

        execute(deleteQueueAuthrequest);

        PCFMessage setQueueAliasAuthrequest = new PCFMessage(MQConstants.MQCMD_SET_AUTH_REC);
        setQueueAliasAuthrequest.addParameter(MQConstants.MQCACF_AUTH_PROFILE_NAME, queue.getAlias());
        setQueueAliasAuthrequest.addParameter(MQConstants.MQIACF_OBJECT_TYPE, MQConstants.MQOT_Q);
        setQueueAliasAuthrequest.addParameter(MQConstants.MQIACF_AUTH_ADD_AUTHS, listAddQueueAuth);
        setQueueAliasAuthrequest.addParameter(MQConstants.MQCACF_GROUP_ENTITY_NAMES, getGroupList("mqusers"));

        execute(setQueueAliasAuthrequest);

        PCFMessage setQueueAuthrequest = new PCFMessage(MQConstants.MQCMD_SET_AUTH_REC);
        setQueueAuthrequest.addParameter(MQConstants.MQCACF_AUTH_PROFILE_NAME, queue.getName());
        setQueueAuthrequest.addParameter(MQConstants.MQIACF_OBJECT_TYPE, MQConstants.MQOT_Q);
        setQueueAuthrequest.addParameter(MQConstants.MQIACF_AUTH_ADD_AUTHS, listAddQueueAuth);
        setQueueAuthrequest.addParameter(MQConstants.MQCACF_GROUP_ENTITY_NAMES, getGroupList("mqusers"));

        execute(setQueueAuthrequest);

        log.info("Updated queue authorization for " + queue.getName() + " and " + queue.getAlias());
    }

    @Deprecated
    public void setGeneralAuthorizations() {
        int[] listDeleteAuth = new int[1];
        listDeleteAuth[0] = MQConstants.MQAUTH_ALL;

        int[] objectTypes = new int[9];
        objectTypes[0] = MQConstants.MQOT_CHANNEL;
        objectTypes[1] = MQConstants.MQOT_LISTENER;
        objectTypes[2] = MQConstants.MQOT_PROCESS;
        objectTypes[3] = MQConstants.MQOT_SERVICE;
        objectTypes[4] = MQConstants.MQOT_AUTH_INFO;
        objectTypes[5] = MQConstants.MQOT_REMOTE_Q_MGR_NAME;
        objectTypes[6] = MQConstants.MQOT_COMM_INFO;
        objectTypes[7] = MQConstants.MQOT_Q;
        objectTypes[8] = MQConstants.MQOT_Q_MGR;

        for (int objectType : objectTypes) {
            PCFMessage deleteQueueAuthrequest = new PCFMessage(MQConstants.MQCMD_SET_AUTH_REC);
            deleteQueueAuthrequest.addParameter(MQConstants.MQCACF_AUTH_PROFILE_NAME, "**");
            deleteQueueAuthrequest.addParameter(MQConstants.MQIACF_OBJECT_TYPE, objectType);
            deleteQueueAuthrequest.addParameter(MQConstants.MQIACF_AUTH_REMOVE_AUTHS, listDeleteAuth);
            deleteQueueAuthrequest.addParameter(MQConstants.MQCACF_GROUP_ENTITY_NAMES, getGroupList("mqusers"));

            execute(deleteQueueAuthrequest);
        }

        PCFMessage deleteQueueAuthrequest = new PCFMessage(MQConstants.MQCMD_SET_AUTH_REC);
        deleteQueueAuthrequest.addParameter(MQConstants.MQCACF_AUTH_PROFILE_NAME, "SYSTEM.BASE.TOPIC");
        deleteQueueAuthrequest.addParameter(MQConstants.MQIACF_OBJECT_TYPE, MQConstants.MQOT_TOPIC);
        deleteQueueAuthrequest.addParameter(MQConstants.MQIACF_AUTH_REMOVE_AUTHS, listDeleteAuth);
        deleteQueueAuthrequest.addParameter(MQConstants.MQCACF_GROUP_ENTITY_NAMES, getGroupList("mqusers"));
        execute(deleteQueueAuthrequest);

        int[] listAddAuth = new int[3];
        listAddAuth[0] = MQConstants.MQAUTH_CONNECT;
        listAddAuth[1] = MQConstants.MQAUTH_INQUIRE;
        listAddAuth[2] = MQConstants.MQAUTH_DISPLAY;

        PCFMessage addQmgrAuthrequest = new PCFMessage(MQConstants.MQCMD_SET_AUTH_REC);
        addQmgrAuthrequest.addParameter(MQConstants.MQCACF_AUTH_PROFILE_NAME, "**");
        addQmgrAuthrequest.addParameter(MQConstants.MQIACF_OBJECT_TYPE, MQConstants.MQOT_Q_MGR);
        addQmgrAuthrequest.addParameter(MQConstants.MQIACF_AUTH_ADD_AUTHS, listAddAuth);
        addQmgrAuthrequest.addParameter(MQConstants.MQCACF_GROUP_ENTITY_NAMES, getGroupList("mqusers"));
        execute(addQmgrAuthrequest);

        int[] listDlqAuth = new int[5];
        listDlqAuth[0] = MQConstants.MQAUTH_DISPLAY;
        listDlqAuth[0] = MQConstants.MQAUTH_BROWSE;
        listDlqAuth[0] = MQConstants.MQAUTH_INPUT;
        listDlqAuth[0] = MQConstants.MQAUTH_INQUIRE;
        listDlqAuth[0] = MQConstants.MQAUTH_PASS_ALL_CONTEXT;

        String[] listDlq = new String[2];
        listDlq[0] = "**.DLQ";
        listDlq[1] = "DLQ.**";

        for (String dlq : listDlq) {
            PCFMessage addDlqAuthrequest = new PCFMessage(MQConstants.MQCMD_SET_AUTH_REC);
            addDlqAuthrequest.addParameter(MQConstants.MQCACF_AUTH_PROFILE_NAME, dlq);
            addDlqAuthrequest.addParameter(MQConstants.MQIACF_OBJECT_TYPE, MQConstants.MQOT_Q);
            addDlqAuthrequest.addParameter(MQConstants.MQIACF_AUTH_ADD_AUTHS, listDlqAuth);
            addDlqAuthrequest.addParameter(MQConstants.MQCACF_GROUP_ENTITY_NAMES, getGroupList("mqusers"));
            execute(addQmgrAuthrequest);
        }
        log.info("Updated authorization for (prevention of accumulated authorities)");

    }

    public void delete(MqQueue queue) {
        PCFMessage deleteRequest = new PCFMessage(MQConstants.MQCMD_DELETE_Q);
        deleteRequest.addParameter(MQConstants.MQCA_Q_NAME, queue.getName());
        execute(deleteRequest);
        log.info("Deleted queue {}", queue.getName());

        PCFMessage deleteAliasRequest = new PCFMessage(MQConstants.MQCMD_DELETE_Q);
        deleteAliasRequest.addParameter(MQConstants.MQCA_Q_NAME, queue.getAlias());
        execute(deleteAliasRequest);
        log.info("Deleted queue alias {}", queue.getAlias());
    }

    @SuppressWarnings("unchecked")
    public void print(MqQueue queue) {
        PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q);
        request.addParameter(MQConstants.MQCA_Q_NAME, queue.getName());
        request.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_ALL);

        PCFMessage[] responses = execute(request);
        System.out.println();
        System.out.println("------------------ Queue data -------------------------");
        Enumeration<PCFParameter> pcfMessage = responses[0].getParameters();
        while (pcfMessage.hasMoreElements()) {
            PCFParameter param = pcfMessage.nextElement();
            String value = param.getStringValue().trim();
            if (value != null && !value.isEmpty()) {

                System.out.println("   " + StringUtils.rightPad(param.getParameterName(), 40) + " " + param.getStringValue());
            }
        }
        System.out.println("--------------------------------------------------");

    }

    public boolean exists(MqQueue queue) {
        PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q_NAMES);
        request.addParameter(MQConstants.MQCA_Q_NAME, queue.getName());
        request.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_ALL);

        PCFMessage[] responses = execute(request);

        String[] names = (String[]) responses[0].getParameterValue(MQConstants.MQCACF_Q_NAMES);
        for (int i = 0; i < names.length; i++)
        {
            log.debug("Found Queue: {} ", names[i]);
        }
        return names.length != 0;
    }

    public void createOrUpdate(MqChannel channel) {
        log.info("Create or update channel {}", channel.getName());
        if (!exists(channel)) {
            PCFMessage createChannelrequest = new PCFMessage(MQConstants.MQCMD_CREATE_CHANNEL);
            createChannelrequest.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
            createChannelrequest.addParameter(MQConstants.MQIACH_CHANNEL_TYPE, channel.getType());
            createChannelrequest.addParameter(MQConstants.MQCACH_DESC, channel.getDescription());
            if (channel.getType() == MQConstants.MQCHT_SENDER) {
                createChannelrequest.addParameter(MQConstants.MQCACH_XMIT_Q_NAME, channel.getXmitQueue());
                createChannelrequest.addParameter(MQConstants.MQCACH_CONNECTION_NAME, channel.getConnectionName());
                log.info("Transmit queue:" + channel.getXmitQueue() + " and Connection name: " + channel.getConnectionName());
            }

            // Settings for secure MQ:
            // createChannelrequest.addParameter(MQConstants.MQCACH_SSL_CIPHER_SPEC, "RC4_MD5_US");
            // createChannelrequest.addParameter(MQConstants.MQCACH_MCA_USER_ID, "srvappserver");

            execute(createChannelrequest);
            log.info("Created channel {}", channel.getName());
        } else {
            log.info("Channel {} exists. Updating", channel.getName());
            PCFMessage updateRequest = new PCFMessage(MQConstants.MQCMD_CHANGE_CHANNEL);
            updateRequest.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
            updateRequest.addParameter(MQConstants.MQIACH_CHANNEL_TYPE, MQConstants.MQCHT_SVRCONN);
            updateRequest.addParameter(MQConstants.MQCACH_DESC, channel.getDescription() + " updated");

            execute(updateRequest);
            log.info("Updated channel " + channel.getName());
        }
    }

    public void resetChannelSequence(MqChannel channel, int sequenceNo) {
        log.info("Reset channel sequence number to " + sequenceNo);
        if (exists(channel)) {
            PCFMessage resetChannelrequest = new PCFMessage(MQConstants.MQCMD_RESET_CHANNEL);
            resetChannelrequest.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
            resetChannelrequest.addParameter(MQConstants.MQIACH_MSG_SEQUENCE_NUMBER, sequenceNo);
            execute(resetChannelrequest);
        }
    }

    public void stopChannel(MqChannel channel) {
        log.info("Stopping channel " + channel.getName());
        if (exists(channel)) {
            try {
                PCFMessage stopChannelrequest = new PCFMessage(MQConstants.MQCMD_STOP_CHANNEL);
                stopChannelrequest.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
                stopChannelrequest.addParameter(MQConstants.MQIACF_MODE, MQConstants.MQMODE_FORCE);
                execute(stopChannelrequest);
            } catch (Exception e) {
                log.info("Channel is not active");
            }

        }
    }

    public void resolveChannel(MqChannel channel) {
        log.info("Resolving channel " + channel.getName());
        if (exists(channel)) {
            PCFMessage resolveChannelrequest = new PCFMessage(MQConstants.MQCMD_RESOLVE_CHANNEL);
            resolveChannelrequest.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
            resolveChannelrequest.addParameter(MQConstants.MQIACH_IN_DOUBT, MQConstants.MQIDO_BACKOUT);
            execute(resolveChannelrequest);
        }
    }

    public void setChannelAuthorization(MqChannel channel, String ipRange, String username) {
        PCFMessage setChannelAuthrequest = new PCFMessage(MQConstants.MQCMD_SET_CHLAUTH_REC);
        setChannelAuthrequest.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
        setChannelAuthrequest.addParameter(MQConstants.MQIACF_CHLAUTH_TYPE, MQConstants.MQCAUT_USERMAP);
        setChannelAuthrequest.addParameter(MQConstants.MQIACF_ACTION, MQConstants.MQACT_REPLACE);
        setChannelAuthrequest.addParameter(MQConstants.MQCACH_CONNECTION_NAME, ipRange);
        setChannelAuthrequest.addParameter(MQConstants.MQCACH_CLIENT_USER_ID, username);
        setChannelAuthrequest.addParameter(MQConstants.MQCACH_MCA_USER_ID, username);
        setChannelAuthrequest.addParameter(MQConstants.MQCA_CHLAUTH_DESC, channel.getDescription());

        execute(setChannelAuthrequest);
        log.info("Updated channel and authentication object for " + channel.getName());
    }

    public void get(MqChannel channel) throws Exception {
        PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_CHANNEL);
        request.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());

        PCFMessage[] channelResponses = execute(request);
        System.out.println("Description: " + channelResponses[0].getParameterValue(MQConstants.MQCACH_DESC));

        PCFMessage channelInquiry = new PCFMessage(MQConstants.MQCMD_INQUIRE_CHLAUTH_RECS);
        channelInquiry.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
        PCFMessage[] channelAuthResponses = execute(channelInquiry);
        log.info("Valid IP-addresses for " + channelAuthResponses[0].getStringParameterValue(MQConstants.MQCACH_CHANNEL_NAME) + ": "
                + channelAuthResponses[0].getStringParameterValue(MQConstants.MQCACH_CONNECTION_NAME));
    }

    @SuppressWarnings("unchecked")
    public void print(MqChannel channel) {
        PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_CHANNEL);
        request.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
        request.addParameter(MQConstants.MQIACH_CHANNEL_TYPE, channel.getType());

        PCFMessage[] responses = execute(request);
        System.out.println();
        System.out.println("---------- Channel data -------------------------");
        Enumeration<PCFParameter> pcfMessage = responses[0].getParameters();
        while (pcfMessage.hasMoreElements()) {
            PCFParameter param = pcfMessage.nextElement();
            String value = param.getStringValue().trim();
            if (value != null && !value.isEmpty()) {

                System.out.println("   " + StringUtils.rightPad(param.getParameterName(), 40) + " " + param.getStringValue());
            }

        }
        System.out.println("--------------------------------------------------");

    }

    public void delete(MqChannel channel) {
        PCFMessage deleteRequest = new PCFMessage(MQConstants.MQCMD_DELETE_CHANNEL);
        deleteRequest.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
        execute(deleteRequest);

        log.info("Deleted channel {}", channel.getName());
    }

    public void deleteChannelAuthentication(MqChannel channel, String ipRange, String username) {
        PCFMessage deleteChannelAuthrequest = new PCFMessage(MQConstants.MQCMD_SET_CHLAUTH_REC);
        deleteChannelAuthrequest.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
        deleteChannelAuthrequest.addParameter(MQConstants.MQIACF_CHLAUTH_TYPE, MQConstants.MQCAUT_USERMAP);
        deleteChannelAuthrequest.addParameter(MQConstants.MQIACF_ACTION, MQConstants.MQACT_REMOVE);
        deleteChannelAuthrequest.addParameter(MQConstants.MQCACH_CONNECTION_NAME, ipRange);
        deleteChannelAuthrequest.addParameter(MQConstants.MQCACH_CLIENT_USER_ID, username);

        execute(deleteChannelAuthrequest);
        log.info("Deleted channel authentication object {}", channel.getName());
    }

    public String[] getGroupList(String group) {
        String[] listGroup = new String[1];
        listGroup[0] = group;
        return listGroup;
    }

    public boolean exists(MqChannel channel) {
        PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_CHANNEL_NAMES);
        request.addParameter(MQConstants.MQCACH_CHANNEL_NAME, channel.getName());
        request.addParameter(MQConstants.MQIACH_CHANNEL_TYPE, MQConstants.MQCHT_SVRCONN);

        PCFMessage[] responses = execute(request);
        if (responses[0].getParameterCount() != 0) {
            String[] names = (String[]) responses[0].getParameterValue(MQConstants.MQCACH_CHANNEL_NAMES);

            for (int i = 0; i < names.length; i++)
            {
                log.debug("Found channel: {}", names[i]);
            }
            return true;
        } else {
            return false;
        }
    }

    private PCFMessage[] execute(PCFMessage request) {
       return queueManager.execute(request);
    }



    @Override
    public void close() throws Exception {
        queueManager.close();
    }

   
}
