package no.nav.aura.basta.backend.vmware.orchestrator;

import no.nav.aura.basta.domain.input.EnvironmentClass;

import com.google.common.collect.ImmutableMap;

public enum OrchestratorEnvironmentClass {
	utv, test, preprod, qa, prod;
	
	private static ImmutableMap<EnvironmentClass, OrchestratorEnvironmentClass> standardMapping = 
			ImmutableMap.of(
			EnvironmentClass.u, utv,
			EnvironmentClass.t, test, 
			EnvironmentClass.q, qa, 
			EnvironmentClass.p, prod);


	public static OrchestratorEnvironmentClass convert(EnvironmentClass environmentClass, Boolean isMultisite) {
		if (isMultisite && environmentClass.equals(EnvironmentClass.q)) {
			return preprod;
		}
		return standardMapping.get(environmentClass);

	}
}
