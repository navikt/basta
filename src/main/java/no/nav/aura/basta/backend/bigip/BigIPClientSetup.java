package no.nav.aura.basta.backend.bigip;

import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import no.nav.aura.basta.backend.BigIPClient;
import no.nav.aura.basta.backend.FasitRestClient;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.SecretPayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.domain.input.bigip.BigIPOrderInput;

@Component
public class BigIPClientSetup {

    
    @Inject
    private FasitRestClient fasitRestClient;
    
    @Inject
    private ActiveBigIPInstanceFinder activeInstanceFinder;


    public BigIPClient setupBigIPClient(BigIPOrderInput input) {
    	ResourcePayload loadBalancer = getBigIPResource(input).getResources().get(0);
        
    	String username = loadBalancer.getProperties().get("username");
    	SecretPayload secret = loadBalancer.getSecrets().get("password");
    	
    	String password = fasitRestClient.getFasitSecret(secret.ref.toString());
    	
        String activeInstance = activeInstanceFinder.getActiveBigIPInstance(loadBalancer, username, password);
        if (activeInstance == null) {
            throw new RuntimeException("Unable to find any active BIG-IP instance");
        }
        return new BigIPClient(activeInstance, username, password);
    }

    private ResourcesListPayload getBigIPResource(BigIPOrderInput input) {
		ScopePayload scope = new ScopePayload()
				.environmentClass(input.getEnvironmentClass())
				.environment(input.getEnvironmentName())
				.zone(input.getZone())
				.application(input.getApplicationName());
		
		return fasitRestClient.findFasitResources(ResourceType.LoadBalancer, "bigip", scope);
	}
}