package no.nav.aura.basta.backend.bigip;

import no.nav.aura.basta.backend.BigIPClient;
import no.nav.aura.basta.backend.fasit.payload.ResourcePayload;
import no.nav.aura.basta.backend.fasit.payload.ResourceType;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.bigip.BigIPOrderInput;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class BigIPClientSetup {

    @Inject
    private FasitRestClient fasitRestClient;
    @Inject
    private ActiveBigIPInstanceFinder activeInstanceFinder;


    public BigIPClient setupBigIPClient(BigIPOrderInput input) {
        ResourcePayload loadBalancer = getFasitResource(ResourceType.loadbalancer, "bigip", input);

        String username = loadBalancer. get("username");
        String password = fasitRestClient.getSecret(loadBalancer.getropertyUri("password"));

        String activeInstance = activeInstanceFinder.getActiveBigIPInstance(loadBalancer, username, password);
        if (activeInstance == null) {
            throw new RuntimeException("Unable to find any active BIG-IP instance");
        }
        return new BigIPClient(activeInstance, username, password);
    }

    private ResourcePayload getFasitResource(ResourceType type, String alias, BigIPOrderInput input) {
        Domain domain = Domain.findBy(input.getEnvironmentClass(), input.getZone());
        return fasitRestClient.getResource(input.getEnvironmentName(), alias, type, DomainDO.fromFqdn(domain.getFqn()), input.getApplicationName());
    }
}
