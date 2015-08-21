package no.nav.aura.basta.util;

import com.google.common.collect.Lists;
import com.google.common.primitives.Chars;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


public class PasswordGeneratorTest {

    @Test
    public void generatesTestAtLength() {
        assertThat(PasswordGenerator.generate(14).length(), is(14));
    }

    @Test
    public void containsOnlyLettersAndNumbers(){
        for (int i = 0; i < 30; i++) {
            List<Character> actual = Chars.asList(PasswordGenerator.generate(20).toCharArray());
            assertThat(actual, not(hasItems('~', '#', '@', '*', '+', '%', '{','}','<','>','[', ']', '|', '“', '”', '\\', '_', '^')));
        }

    }
}