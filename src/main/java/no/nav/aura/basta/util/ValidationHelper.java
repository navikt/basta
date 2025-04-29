package no.nav.aura.basta.util;

import java.io.InputStream;
import java.util.*;

import org.jboss.resteasy.spi.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.JsonValidator;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MultivaluedMap;

public class ValidationHelper {

    private static final Logger logger = LoggerFactory.getLogger(ValidationHelper.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();


    public static String prettifyJson(String uglyJson) {
        final JsonElement jsonElement = com.google.gson.JsonParser.parseString(uglyJson);
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonElement);
    }

    public static Set<ValidationMessage> validate(String schemaPath, Map<String, ?> request) {
    	try {
    		final String jsonString = new Gson().toJson(request);
    		logger.info(jsonString);
    	
    		// Load schema
    		InputStream schemaStream = JsonValidator.class.getResourceAsStream(schemaPath);
    		 if (schemaStream == null) {
                 throw new IllegalArgumentException("Schema not found at path: " + schemaPath);
    		 }
    		 
    		JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V7);
	    	JsonSchema schema = jsonSchemaFactory.getSchema(schemaStream);
            
        	JsonNode jsonNode = objectMapper.readTree(jsonString);

        	return schema.validate(jsonNode);
        } catch (Exception e) {
            throw new RuntimeException("Error during JSON schema validation", e);
        }
    }

    public static void validateRequest(String jsonSchema, Map<String, ?> request) {
    	Set<ValidationMessage> validation;
        try {
            validation = ValidationHelper.validate(jsonSchema, request);
        } catch (RuntimeException e) {
        	logger.error("Unable to validate request: " + request + " against schema " + jsonSchema, e);
            throw new InternalServerErrorException("Unable to validate request");
        }
        if (!validation.isEmpty()) {
        	
            StringBuffer errormessage = new StringBuffer("Input did not pass validation. \n");
            validation.forEach(pr -> errormessage.append(pr.getMessage() + "\n"));
            throw new BadRequestException(errormessage.toString());
        }
    }

    public static void  validateAllParams(MultivaluedMap<String,String> request){
        validateAllParams(queryParamsAsMap(request));
    }

    public static void  validateAllParams(Map<String,String> request){
        validateRequiredParams(request, request.keySet().stream().toArray(s -> new String[s]));
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


    public static HashMap<String, String> queryParamsAsMap(MultivaluedMap<String, String> mvMap) {
        HashMap<String, String> map = new HashMap<>();
        for (String key : mvMap.keySet()) {
            map.put(key, mvMap.getFirst(key));
        }
        return map;
    }
}
