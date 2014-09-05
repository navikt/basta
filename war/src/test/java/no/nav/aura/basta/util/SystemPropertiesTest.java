package no.nav.aura.basta.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

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


    @Test
    public void test_uri() throws Exception {
        URI fasitURI = new URI("https://fasit.adeo.no:80/conf/resources/540997");
        System.out.println(fasitURI.getAuthority());
        System.out.println(fasitURI.getFragment());
        System.out.println(fasitURI.getHost());
        System.out.println(fasitURI.getPath());
        System.out.println(fasitURI.getPort());
        System.out.println(fasitURI.getScheme());
        URI build = UriBuilder.fromUri(fasitURI).replacePath("lookup").queryParam("type", "node").build();
        System.out.println(build);


        assertThat(true, is(true));
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
