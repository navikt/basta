package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.Zone;
import no.nav.aura.basta.domain.input.bigip.BigIPOrderInput;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.envconfig.client.DomainDO;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

import java.util.Collection;
import java.util.Optional;

import static no.nav.aura.basta.domain.input.vm.OrderStatus.FAILURE;

public abstract class AbstractVmOrderRestService {
    protected OrderRepository orderRepository;
    protected OrchestratorClient orchestratorClient;
    protected FasitRestClient fasitClient;


    public AbstractVmOrderRestService(OrderRepository orderRepository, OrchestratorClient orchestratorClient) {
        this.orderRepository = orderRepository;
        this.orchestratorClient = orchestratorClient;
    }

    public AbstractVmOrderRestService(OrderRepository orderRepository, OrchestratorClient orchestratorClient, FasitRestClient fasitClient) {
        this(orderRepository, orchestratorClient);
        this.fasitClient = fasitClient;
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    protected Order executeProvisonOrder(final Order order, OrchestatorRequest request) {
        order.addStatuslogInfo("Calling Orchestrator for provisioning");
        Optional<String> runningWorkflowUrl = orchestratorClient.provision(request);
        runningWorkflowUrl.ifPresent(s -> order.setExternalId(s.toString()));

        if (!runningWorkflowUrl.isPresent()) {
            order.setStatus(FAILURE);
        }

        return orderRepository.save(order);
    }

    protected ResourceElement getFasitResource(ResourceTypeDO type, String alias, VMOrderInput input, Zone zone) {
        Domain domain = Domain.findBy(input.getEnvironmentClass(), zone);
        DomainDO.EnvClass envClass = DomainDO.EnvClass.valueOf(input.getEnvironmentClass().name());
        Collection<ResourceElement> resources = fasitClient.findResources(envClass, input.getEnvironmentName(), DomainDO.fromFqdn(domain.getFqn()), null, type, alias);
        return resources.isEmpty() ? null : resources.iterator().next();
    }

    protected ResourceElement getFasitResource(ResourceTypeDO type, String alias, VMOrderInput input) {
        return getFasitResource(type, alias, input, input.getZone());
    }

    protected String getWasLdapBindUserForFss(VMOrderInput input, String property) {
        String alias = "wasLdapUser";
        ResourceTypeDO type = ResourceTypeDO.Credential;

        Domain domain = Domain.findBy(input.getEnvironmentClass(), Zone.fss);
        DomainDO.EnvClass envClass = DomainDO.EnvClass.valueOf(input.getEnvironmentClass().name());
        Collection<ResourceElement> resources = fasitClient.findResources(envClass, input.getEnvironmentName(), DomainDO.fromFqdn(domain.getFqn()), null, type, alias);
        return resources.isEmpty() ? null : resolveProperty(resources.iterator().next(), property);
    }

    protected String resolveProperty(ResourceElement resource, String propertyName) {
        for (PropertyElement property : resource.getProperties()) {
            if (property.getName().equals(propertyName)) {
                if (property.getType() == PropertyElement.Type.SECRET) {
                    return fasitClient.getSecret(property.getRef());
                }
                return property.getValue();
            }
        }
        throw new RuntimeException("Property " + propertyName + " not found for Fasit resource " + resource.getAlias());
    }
}