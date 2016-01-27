package no.nav.aura.basta.backend.mq;

import java.util.Date;

import com.ibm.mq.constants.MQConstants;

public class MqChannel {
    private static final int APP_NAME_MAXLENGTH = 14;
    private static final int ENV_NAME_MAXLENGTH = 5;
    private String name;
    private int type=MQConstants.MQCHT_SVRCONN;
//    private String xmitQueueName;
//    private String connectionName;

   

    public MqChannel(String channelName) {
        this.name = channelName;
    }

   
    public static String formatChannelName(String environmentName, String appName) {
        return (formatEnvName(environmentName) + "_" + formatRestrictedLength(appName, APP_NAME_MAXLENGTH)).toUpperCase();
    }

    /** Remove invalid characters and restrict length to maxLength */
    public static String formatRestrictedLength(String string, int maxLength) {
        String justWords = string.replaceAll("[\\W_]", "");
        if (justWords.length() > maxLength) {
            return justWords.substring(0, maxLength);
        }
        return justWords;
    }

    /**
     * Formats string with max length of ENV_NAME_MAXLENGTH by removing invalid characters and shortning the string with the
     * suffix as the most significant. Input is expected to have format "something-suffix"
     */
    private static String formatEnvName(String string) {
        String justWords = string.replaceAll("[\\W_]", "");
        if (justWords.length() > ENV_NAME_MAXLENGTH) {
            int suffixindex = string.lastIndexOf("-");
            if (suffixindex != -1) {
                String suffix = string.substring(suffixindex + 1);
                int suffixlength = suffix.length();
                if (suffix.length() <= ENV_NAME_MAXLENGTH) {
                    return justWords.substring(0, ENV_NAME_MAXLENGTH - suffixlength) + suffix;
                } else {
                    throw new IllegalArgumentException("Environmentname " + string + " has invalid format");
                }
            }
            return justWords.substring(0, ENV_NAME_MAXLENGTH);
        }
        return justWords;
    }

    public String getName() {
        return name;
    }

    public boolean hasValidName() {
        return name.length() <= 20;
    }

    public String getDescription() {
        return "generated on " + new Date();
    }

    public String[] getUserList() {
        return null;
    }

    public int getType() {
        return type;
    }
}
