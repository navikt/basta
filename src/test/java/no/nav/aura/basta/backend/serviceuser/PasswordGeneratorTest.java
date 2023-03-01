package no.nav.aura.basta.backend.serviceuser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PasswordGeneratorTest {
    @Test
    public void passwordHasSpecifiedLenghtAndContainsAtLeasOneUppercaseOneLowercaseAndANumber() {
        String minimalPassword = PasswordGenerator.generate(3);
        assertEquals(minimalPassword.length(), 3);
        assertEquals(countUppercase(minimalPassword), 1);
        assertEquals(countLowercase(minimalPassword), 1);
        assertEquals(countNumber(minimalPassword), 1);


        String longPassword = PasswordGenerator.generate(22);
        assertEquals(longPassword.length(), 22);
        assertTrue(countUppercase(longPassword) >= 1);
        assertTrue(countLowercase(longPassword) >= 1);
        assertTrue(countNumber(longPassword) >= 1);
    }

    private long countUppercase(String string) {
        return string.chars().filter(Character::isUpperCase).count();
    }

    private long countLowercase(String string) {
        return string.chars().filter(Character::isLowerCase).count();
    }

    private long countNumber(String string) {
        return string.chars().filter(Character::isDigit).count();
    }


}
