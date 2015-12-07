package no.nav.aura.basta.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonHelper {
    public static String prettifyJson(String uglyJson) {
        final JsonParser jsonParser = new JsonParser();
        final JsonElement jsonElement = jsonParser.parse(uglyJson);
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonElement);
    }
}
