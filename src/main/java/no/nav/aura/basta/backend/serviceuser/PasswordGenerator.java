package no.nav.aura.basta.backend.serviceuser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.*;

public class PasswordGenerator {
    static final String lowercase = "abcdefghijklmnopqrstuvwxyz";
    static final String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final String numbers = "0123456789";
    static final String combined = lowercase + uppercase + numbers;

    public static final String generate(int len) {
        Random random = new Random();

        StringBuilder stringBuilder = new StringBuilder(len);
        stringBuilder.append(lowercase.charAt(random.nextInt(lowercase.length())));
        stringBuilder.append(uppercase.charAt(random.nextInt(uppercase.length())));
        stringBuilder.append(numbers.charAt(random.nextInt(numbers.length())));

        for (int i = 3; i < len; i++) {
            stringBuilder.append(combined.charAt(random.nextInt(combined.length())));
        }

        return shuffle(stringBuilder.toString());
    }

    // Shuffle password so that it is not predictable that password always starts
    // with a lowercase character, an uppercase character and a number in that order
    private static String shuffle(String stringToShuffle) {
        List<String> characterList = Arrays.asList(stringToShuffle.split(""));
        Collections.shuffle(characterList, new Random());

        return characterList.stream().collect(joining());
    }
}
