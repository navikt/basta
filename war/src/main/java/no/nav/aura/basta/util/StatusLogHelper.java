package no.nav.aura.basta.util;

import org.apache.commons.lang.StringUtils;

public class StatusLogHelper {

    public static String abbreviateExceptionMessage(Exception e) {
        if (e.getMessage() != null && e.getMessage().length() > 3) {
            return ": " + StringUtils.abbreviate(e.getMessage(), 158);
        }
        return e.getMessage();
    }
}
