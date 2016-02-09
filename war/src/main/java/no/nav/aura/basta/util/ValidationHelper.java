package no.nav.aura.basta.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.InternalServerErrorException;
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

public class ValidationHelper {

    private static final Logger log = LoggerFactory.getLogger(ValidationHelper.class);

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

    public static void validateRequest(String jsonSchema, Map<String, ?> request) {
        ProcessingReport validation;
        try {
            validation = ValidationHelper.validate(jsonSchema, request);
        } catch (RuntimeException e) {
            log.error("Unable to validate request: " + request + " against schema " + jsonSchema, e);
            throw new InternalServerErrorException("Unable to validate request");
        }
        if (!validation.isSuccess()) {
            StringBuffer errormessage = new StringBuffer("Input did not pass validation. \n");
            validation.forEach(pr -> errormessage.append(pr.getMessage() + "\n"));
            throw new BadRequestException(errormessage.toString());
        }
    }

    public static void validateRequiredParams(Map<String, String> request, String... keys) {
        List<String> validationErrors = new ArrayList<>();
        for (String key : keys) {
            if (!request.containsKey(key) || request.get(key) == null || request.get(key).isEmpty()) {
                validationErrors.add(key);
            }
        }
        if (!validationErrors.isEmpty()) {
            StringBuffer errormessage = new StringBuffer("Input did not pass validation. \n");
            validationErrors.forEach(key -> errormessage.append("Param: '" + key + "' is required\n"));
            throw new BadRequestException(errormessage.toString());
        }
    }
}
