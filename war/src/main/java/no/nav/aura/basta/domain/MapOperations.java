package no.nav.aura.basta.domain;

import java.util.HashMap;
import java.util.Map;

import no.nav.aura.basta.util.Tuple;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;


public class MapOperations {

    protected Map<String,String> map;

    public MapOperations(Map<String, String> map) {
        this.map = map;
    }

    public void put(String key, String value){
        map.put(key, value);
    }

    public void put(String key, int value) {
        map.put(key, String.valueOf(value));
    }

    public void put (Tuple<String, String> ...tuples){
        for (Tuple<String,String> tuple : tuples) {
            map.put(tuple.fst, tuple.snd);
        }
    }

    public Optional<String> getOptional(String key) {
        return Optional.fromNullable(map.get(key));
    }

    public String get(String key){
        return getOptional(key).orNull();
    }

    public static MapOperations single(String key, String value) {
        Map<String, String> input = new HashMap<>();
        input.put(key, value);
        return new MapOperations(input);
    }

    public static MapOperations single(String key, Enum value) {
        Map<String, String> input = Maps.newHashMap();
        input.put(key, value.name());
        return new MapOperations(input);
    }

    public <T extends Enum<T>> T getEnumOrNull(Class<T> enumType, String key){
        return getEnumOr(enumType, key, null);
    }

    public <T extends Enum<T>> T getEnumOr(Class<T> enumType, String key, T defaultValue) {
        return getOptional(key).isPresent() ? Enum.valueOf(enumType, get(key)) : defaultValue;
    }

    public Integer getIntOrNull(String key){
        return getOptional(key).isPresent() ? Integer.parseInt(get(key)) : null;
    }


    public Map<String, String> copy() {
        return Maps.newHashMap(map);
    }

    static <T extends MapOperations> T as(Class<T> resultClass, Map<String, String> constructorParams) {
        try {
            return resultClass.getConstructor(Map.class).newInstance(constructorParams);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
