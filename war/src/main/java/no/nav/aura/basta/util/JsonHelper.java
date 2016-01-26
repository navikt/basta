package no.nav.aura.basta.util;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonHelper {

    private static final Logger log = LoggerFactory.getLogger(JsonHelper.class);

    public static String prettifyJson(String uglyJson) {
        final JsonParser jsonParser = new JsonParser();
        final JsonElement jsonElement = jsonParser.parse(uglyJson);
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonElement);
    }

    public static ProcessingReport validate(String schemaPath, Map<String, ?> request) {
        JsonValidator validator = JsonSchemaFactory.byDefault().getValidator();
        final String jsonString = new Gson().toJson(request);

        try {
            final JsonNode json = JsonLoader.fromString(jsonString);
            final JsonNode schema = JsonLoader.fromResource(schemaPath);
            return validator.validate(schema, json);
        } catch (ProcessingException e) {
            log.error("Invalid JSON Schema", e);
            throw new RuntimeException("Invalid JSON Schema " + schemaPath, e);
        } catch (IOException e) {
            log.error("Unable get JSON Schema", e);
            throw new RuntimeException("Unable get JSON Schema " + schemaPath, e);
        }
    }
}
