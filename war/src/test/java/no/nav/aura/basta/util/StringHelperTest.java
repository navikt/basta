package no.nav.aura.basta.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.primitives.Chars;

public class StringHelperTest {

    @Test
    public void generatesTestAtLength() {
        assertThat(StringHelper.generateRandom(14).length(), is(14));
    }

    @Test
    public void containsOnlyLettersAndNumbers() {
        for (int i = 0; i < 30; i++) {
            List<Character> actual = Chars.asList(StringHelper.generateRandom(20).toCharArray());
            assertThat(actual, not(hasItems('~', '#', '@', '*', '+', '%', '{', '}', '<', '>', '[', ']', '|', '', '', '\\', '_', '^')));
        }
    }
}