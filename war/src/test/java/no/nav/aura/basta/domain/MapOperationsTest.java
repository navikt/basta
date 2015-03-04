package no.nav.aura.basta.domain;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class MapOperationsTest {


    MapOperations map = new MapOperations(Maps.newHashMap());

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