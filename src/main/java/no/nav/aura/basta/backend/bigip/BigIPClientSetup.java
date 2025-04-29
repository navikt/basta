package no.nav.aura.basta.backend.bigip;

import no.nav.aura.basta.backend.BigIPClient;
import no.nav.aura.basta.backend.fasit.deprecated.FasitRestClient;
import no.nav.aura.basta.backend.fasit.deprecated.ResourceElement;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.DomainDO;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.ResourceTypeDO;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.bigip.BigIPOrderInput;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;

@Component
public class BigIPClientSetup {

    @Inject
    private FasitRestClient fasitRestClient;
    @Inject
    private ActiveBigIPInstanceFinder activeInstanceFinder;


    public BigIPClient setupBigIPClient(BigIPOrderInput input) {
        ResourceElement loadBalancer = getFasitResource(ResourceTypeDO.LoadBalancer, "bigip", input);

        String username = loadBalancer.getPropertyString("username");
        String password = fasitRestClient.getSecret(loadBalancer.getPropertyUri("password"));

        String activeInstance = activeInstanceFinder.getActiveBigIPInstance(loadBalancer, username, password);
        if (activeInstance == null) {
            throw new RuntimeException("Unable to find any active BIG-IP instance");
        }
        return new BigIPClient(activeInstance, username, password);
    }

    private ResourceElement getFasitResource(ResourceTypeDO type, String alias, BigIPOrderInput input) {
        Domain domain = Domain.findBy(input.getEnvironmentClass(), input.getZone());
        return fasitRestClient.getResource(input.getEnvironmentName(), alias, type, DomainDO.fromFqdn(domain.getFqn()), input.getApplicationName());
    }
}