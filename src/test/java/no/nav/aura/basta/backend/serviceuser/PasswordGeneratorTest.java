package no.nav.aura.basta.backend.serviceuser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PasswordGeneratorTest {
    @Test
    public void passwordHasSpecifiedLenghtAndContainsAtLeasOneUppercaseOneLowercaseAndANumber() {
        String minimalPassword = PasswordGenerator.generate(3);
        Assertions.assertEquals(minimalPassword.length(), 3);
        Assertions.assertEquals(countUppercase(minimalPassword), 1);
        Assertions.assertEquals(countLowercase(minimalPassword), 1);
        Assertions.assertEquals(countNumber(minimalPassword), 1);


        String longPassword = PasswordGenerator.generate(22);
        Assertions.assertEquals(longPassword.length(), 22);
        Assertions.assertTrue(countUppercase(longPassword) >= 1);
        Assertions.assertTrue(countLowercase(longPassword) >= 1);
        Assertions.assertTrue(countNumber(longPassword) >= 1);
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
