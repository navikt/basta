package no.nav.aura.basta.util;

import com.google.common.primitives.Chars;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;

public class StringHelperTest {

    @Test
    public void generatesTestAtLength() {
        MatcherAssert.assertThat(StringHelper.generateRandom(14).length(), is(14));
    }

    @Test
    public void containsOnlyLettersAndNumbers() {
        for (int i = 0; i < 30; i++) {
            List<Character> actual = Chars.asList(StringHelper.generateRandom(20).toCharArray());
            MatcherAssert.assertThat(actual, not(hasItems('~', '#', '@', '*', '+', '%', '{', '}', '<', '>', '[', ']', '|', '', '', '\\', '_', '^')));
        }
    }
}