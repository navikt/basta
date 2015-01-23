package no.nav.aura.basta.domain ;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import no.nav.aura.basta.persistence.ModelEntity;
import no.nav.aura.basta.util.Tuple;

import javax.persistence.*;
import java.util.Map;


public class Input{

    private  Map<String,String> map;


    public Input(){
        this.map = Maps.newHashMap();
    }

    public Input(Map map){
        this.map = map;
    }

    public void put(String key, String value){
        map.put(key, value);
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

    public static Input single(String key, String value) {
        Map<String, String> input = Maps.newHashMap();
        input.put(key, value);
        return new Input(input);
    }

    public static Input single(String key, Enum value) {
        Map<String, String> input = Maps.newHashMap();
        input.put(key, value.name());
        return new Input(input);
    }

    public <T extends Enum<T>> T getEnumOrNull(Class<T> enumType, String key){
        return getOptional(key).isPresent() ? Enum.valueOf(enumType, get(key)): null;
    }

    public Integer getIntOrNull(String key){
        return getOptional(key).isPresent() ? Integer.parseInt(get(key)) : null;
    }


    public Map<String, String> copy() {
        return Maps.newHashMap(map);
    }
}
