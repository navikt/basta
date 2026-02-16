package no.nav.aura.basta.rest;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.fasit.rest.model.ApplicationListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.infrastructure.Zone;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.domain.input.EnvironmentClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Mockable proxy for fasit lookups
 */
//@Component
//@RestController
@RequestMapping("/rest")
public class FasitLookupService {
	public static final Logger log = LoggerFactory.getLogger(FasitLookupService.class);
	@Autowired
	private RestClient restClient;
	
	private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	public FasitLookupService() {}

	@GetMapping(value = "/v1/fasit/applications", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getApplications() {
		ApplicationListPayload applications = restClient.getAllApplications();
		try {
			return ResponseEntity.ok()
					.cacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS))
					.body(objectMapper.writeValueAsString(applications));
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize applications", e);
		}
	}

	@GetMapping(value = "/v1/fasit/environments", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getEnvironments() {
		try {
			return ResponseEntity.ok()
					.cacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS))
					.body(objectMapper.writeValueAsString(restClient.getAllEnvironments()));
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize environments", e);
		}
	}

	@GetMapping(value = "/v1/fasit/clusters", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Set<String>> getClusters(@RequestParam String environment) {
		throw new UnsupportedOperationException("Clusters is removed in fasit v2 api");
	}

	@GetMapping(value = "/v1/fasit/applicationgroups", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getApplicationGroups() {
		throw new UnsupportedOperationException("Applicationgroups is removed in fasit v2 api");
	}

	@GetMapping(value = "/v1/fasit/resources", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getResources(
			@RequestParam String envClass,
			@RequestParam String environment,
			@RequestParam String application,
			@RequestParam ResourceType type,
			@RequestParam String alias,
			@RequestParam Boolean bestmatch,
			@RequestParam(defaultValue = "false") Boolean usage) {
		throw new UnsupportedOperationException("Use /v1/fasit/resources for resource lookup against fasit v2 api");
	}

	@GetMapping(value = "/v2/fasit/resources", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> findResources(
			@RequestParam String environmentclass,
			@RequestParam(required = false) String environment,
			@RequestParam(required = false) String application,
			@RequestParam(required = false) ResourceType type,
			@RequestParam(required = false) String alias,
			@RequestParam(required = false) Zone zone) {
		if (environmentclass == null) {
			throw new IllegalArgumentException("Missing required parameter environmentclass");
		}

		final ScopePayload scope = new ScopePayload()
				.environmentClass(EnvironmentClass.valueOf(environmentclass))
				.environment(environment)
				.application(application)
				.zone(zone);

		ResourcesListPayload fasitResources = restClient.findFasitResources(type, alias, scope);

		try {
			return ResponseEntity.ok()
					.cacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS))
					.body(objectMapper.writeValueAsString(fasitResources.getResources()));
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize resources", e);
		}
	}

	private static String getSystemPropertyOrThrow(String key, String message) {
		String property = System.getProperty(key);

		if (property == null) {
			throw new IllegalStateException(message);
		}
		return property;
	}

}