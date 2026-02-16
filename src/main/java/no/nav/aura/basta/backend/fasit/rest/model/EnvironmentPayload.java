package no.nav.aura.basta.backend.fasit.rest.model;

import jakarta.validation.constraints.NotNull;
import no.nav.aura.basta.domain.input.EnvironmentClass;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EnvironmentPayload extends EntityPayload {

	@NotNull(message = "name is required")
	public String name;
	@NotNull(message = "environmentclass is required")
	@JsonProperty("environmentclass")
	public EnvironmentClass environmentClass;

	public EnvironmentPayload() {
	}

	public EnvironmentPayload(String name, EnvironmentClass environmentClass) {
		this.name = name;
		this.environmentClass = environmentClass;
	}
}
