package no.nav.aura.basta.util;

import java.security.SecureRandom;

import org.apache.commons.lang3.RandomStringUtils;

public class StringHelper {

    // Using secure random
    public static String generateRandom(int length) {
        return RandomStringUtils.random(length, 0, 0, true, true, null, new SecureRandom());
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }
}
