package no.nav.aura.basta.util;

import java.security.SecureRandom;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomStringGenerator {

    // Using secure random
    public static String generate(int length) {
        return RandomStringUtils.random(length, 0, 0, true, true, null, new SecureRandom());
    }
}
