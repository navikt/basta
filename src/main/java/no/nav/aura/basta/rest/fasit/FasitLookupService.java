package no.nav.aura.basta.rest.fasit;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import no.nav.aura.basta.backend.FasitRestClient;
import no.nav.aura.basta.backend.fasit.rest.model.ApplicationPayload;
import no.nav.aura.basta.backend.fasit.rest.model.EnvironmentPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.infrastructure.Zone;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.domain.input.EnvironmentClass;

/**
 * Mockable proxy for fasit lookups
 */
//@Component
@RestController
@RequestMapping("/rest")
public class FasitLookupService {
	public static final Logger log = LoggerFactory.getLogger(FasitLookupService.class);
	@Autowired
	private FasitRestClient fasitRestClient;
	
	public FasitLookupService() {}

	@GetMapping(value = "/v1/fasit/applications", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ApplicationPayload>> getApplications() {
		List<ApplicationPayload> applications = fasitRestClient.getAllApplications();
		try {
			return ResponseEntity.ok()
					.cacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS))
					.body(applications);
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize applications", e);
		}
	}

	@GetMapping(value = "/v1/fasit/environments", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<EnvironmentPayload>> getEnvironments() {
		List<EnvironmentPayload> environments = fasitRestClient.getAllEnvironments();
		try {
			return ResponseEntity.ok()
					.cacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS))
					.body(environments);
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
	public ResponseEntity<List<ResourcePayload>> findResources(
			@RequestParam String environmentclass,
			@RequestParam(required = false) String environment,
			@RequestParam(required = false) String application,
			@RequestParam(required = false) String type,
			@RequestParam(required = false) String alias,
			@RequestParam(required = false) Zone zone) {
		if (environmentclass == null) {
			throw new IllegalArgumentException("Missing required parameter environmentclass");
		}

		ResourceType resourceType = null;
		if (type != null && !type.isBlank()) {
			resourceType = Arrays.stream(ResourceType.values())
					.filter(rt -> rt.name().equalsIgnoreCase(type.trim()))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException(
							"Unknown ResourceType '" + type + "'. Valid values: " + Arrays.toString(ResourceType.values())));
		}

		final ScopePayload scope = new ScopePayload()
				.environmentClass(EnvironmentClass.valueOf(environmentclass))
				.environment(environment)
				.application(application)
				.zone(zone);

		List<ResourcePayload> fasitResources = fasitRestClient.findFasitResources(resourceType, alias, scope);

		try {
			return ResponseEntity.ok()
					.cacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS))
					.body(fasitResources);
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize resources", e);
		}
	}

}