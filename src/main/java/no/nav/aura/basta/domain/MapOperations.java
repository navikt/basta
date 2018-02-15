package no.nav.aura.basta.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.aura.basta.util.Tuple;


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

    @SuppressWarnings("unchecked")
    public void put (Tuple<String, String> ...tuples){
        for (Tuple<String,String> tuple : tuples) {
            map.put(tuple.fst, tuple.snd);
        }
    }

    public Optional<String> getOptional(String key) {
        return Optional.ofNullable(map.get(key));
    }

    public String get(String key){
        return getOptional(key).orElse(null);
    }

    public static MapOperations single(String key, String value) {
        Map<String, String> input = new HashMap<>();
        input.put(key, value);
        return new MapOperations(input);
    }

    @SuppressWarnings("rawtypes")
    public static MapOperations single(String key, Enum value) {
        Map<String, String> input = new HashMap<>();
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
    
    public Integer getIntOr(String key, int defaultValue){
        return getOptional(key).isPresent() ? Integer.parseInt(get(key)) : defaultValue;
    }


    public Map<String, String> copy() {
        return new HashMap<String, String>(map);
    }

    static <T extends MapOperations> T as(Class<T> resultClass, Map<String, String> constructorParams) {
        try {
            return resultClass.getConstructor(Map.class).newInstance(constructorParams);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
