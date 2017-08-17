package no.nav.aura.basta.util;

import org.apache.commons.codec.binary.Base64;

/**
 * Created by R137915 on 8/17/17.
 */
public class PasswordHelper {
    public static String decodePassword(String password) {
        byte[] decodedBytes = Base64.decodeBase64(password);
        return new String(decodedBytes);
    }
}
