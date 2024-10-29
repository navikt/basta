package no.nav.aura.basta.domain;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

public class MapOperationsTest {


    MapOperations map = new MapOperations(new HashMap<>());

    @Test
    public void test() throws Exception {
        MatcherAssert.assertThat(map.get("balle"), is(nullValue()));
    }

    @Test
    public void te() {
        map.put("fjas", null);
        MatcherAssert.assertThat(map.get("fjas"), is(nullValue()));
    }

    @Test
    public void t() throws Exception {
        map.put("key", "value");
        MatcherAssert.assertThat(map.get("key"), is("value"));
    }




}