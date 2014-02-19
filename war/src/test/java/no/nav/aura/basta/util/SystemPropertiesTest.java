package no.nav.aura.basta.util;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SystemPropertiesTest {

    @SuppressWarnings("serial")
    @Test
    public void testDoWithProperty() {
        SystemPropertiesTest.doWithProperty("asdf", "hei", new Effect() {
            public void perform() {
                assertThat(System.getProperty("asdf"), equalTo("hei"));
                SystemPropertiesTest.doWithProperty("asdf", "hopp", new Effect() {
                    public void perform() {
                        assertThat(System.getProperty("asdf"), equalTo("hopp"));
                    }
                });
                assertThat(System.getProperty("asdf"), equalTo("hei"));
            }
        });
        assertThat(System.getProperty("asdf"), nullValue());
    }

    public static void doWithProperty(String key, String value, Effect effect) {
        String originalProperty = System.getProperty(key);
        try {
            System.setProperty(key, value);
            effect.perform();
        } finally {
            if (originalProperty == null) {
                System.getProperties().remove(key);
            } else {
                System.setProperty(key, originalProperty);
            }
        }
    }

}
