package no.nav.aura.basta.backend;

import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collection;

@Component
public class FasitReadService {


    private static final Logger logger = LoggerFactory.getLogger(FasitReadService.class);

    private final FasitRestClient fasit;

    @Inject
    public FasitReadService(FasitRestClient fasit) {
        this.fasit = fasit;
    }

    public ResourceElement getFasitResource(ResourceTypeDO type, String alias, VMOrderInput input) {
        Domain domain = Domain.findBy(input.getEnvironmentClass(), input.getZone());
        DomainDO.EnvClass envClass = DomainDO.EnvClass.valueOf(input.getEnvironmentClass().name());
        Collection<ResourceElement> resources = fasit.findResources(envClass, input.getEnvironmentName(), DomainDO.fromFqdn(domain.getFqn()), null, type, alias);
        return resources.isEmpty() ? null : resources.iterator().next();
    }

    public String resolveProperty(ResourceElement resource, String propertyName) {
        for (PropertyElement property : resource.getProperties()) {
            if (property.getName().equals(propertyName)) {
                if (property.getType() == PropertyElement.Type.SECRET) {
                    return fasit.getSecret(property.getRef());
                }
                return property.getValue();
            }
        }
        throw new RuntimeException("Property " + propertyName + " not found for Fasit resource " + resource.getAlias());
    }

    public String resolvePropertyFromFasitResource(String propertyName, ResourceTypeDO type, String alias, VMOrderInput input) {
        ResourceElement resourceElement = getFasitResource(type, alias, input);
        return resourceElement == null ? null : resolveProperty(resourceElement, propertyName);

    }

    public String getPasswordForUser(VMOrderInput input, String user) {
        logger.info("Retrieving password for  user " + user);
        return resolvePropertyFromFasitResource("password", ResourceTypeDO.Credential, user, input);

    }
}
