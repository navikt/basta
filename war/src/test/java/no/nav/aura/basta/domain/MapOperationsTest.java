package no.nav.aura.basta.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import org.junit.Test;

public class MapOperationsTest {


    MapOperations map = new MapOperations(new HashMap<String, String>());

    @Test
    public void test() throws Exception {
        assertThat(map.get("balle"), is(nullValue()));
    }

    @Test
    public void te() throws Exception {
        map.put("fjas", null);
        assertThat(map.get("fjas"),is(nullValue()));
    }

    @Test
    public void t() throws Exception {
        map.put("key", "value");
        assertThat(map.get("key"), is("value"));
    }




}