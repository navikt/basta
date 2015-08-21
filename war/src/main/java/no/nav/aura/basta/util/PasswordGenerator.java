package no.nav.aura.basta.util;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;

public class PasswordGenerator {

    //Using secure random
    public static String generate(int length) {
        return RandomStringUtils.random(length, 0, 0, true, true, null, new SecureRandom());
    }
}
