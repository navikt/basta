package no.nav.aura.basta.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class MapBuilder<K,V> {
    private final Map<K, V> map;

    private MapBuilder() {
        this.map = new HashMap<>();
    }

    public static <K, V> MapBuilder<K, V> builder() {
        return new MapBuilder<>();
    }

    public static MapBuilder<String, String> stringMapBuilder() {
        return new MapBuilder<>();
    }

    public static <K, V> Map<K, V> of(K key, V value) {
        HashMap<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }


    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public static Map<String, String> fromProperties(Properties properties) {
        return properties.entrySet().stream().collect(Collectors.toMap(
                e -> String.valueOf(e.getKey()),
                e -> String.valueOf(e.getValue())));

    }

    public MapBuilder<K, V> putAll(Map<K, V> otherMap) {
        map.putAll(otherMap);
        return this;
    }

    public Map<K,V> build() {
        return map;
    }


}

