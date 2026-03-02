package no.nav.aura.basta.rest.vm;

import static no.nav.aura.basta.domain.input.vm.OrderStatus.FAILURE;

import java.util.Optional;

import no.nav.aura.basta.backend.FasitRestClient;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.repository.OrderRepository;

public abstract class AbstractVmOrderRestService {
    protected FasitRestClient fasitRestClient;
    protected OrderRepository orderRepository;
    protected OrchestratorClient orchestratorClient;

    public AbstractVmOrderRestService() {
    }

    public AbstractVmOrderRestService(OrderRepository orderRepository, OrchestratorClient orchestratorClient) {
        this.orderRepository = orderRepository;
        this.orchestratorClient = orchestratorClient;
    }

    public AbstractVmOrderRestService(OrderRepository orderRepository, OrchestratorClient orchestratorClient, FasitRestClient fasitRestClient) {
        this(orderRepository, orchestratorClient);
        this.fasitRestClient = fasitRestClient;
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    protected Order executeProvisionOrder(final Order order, OrchestatorRequest request) {
        order.addStatuslogInfo("Calling Orchestrator for provisioning");
        Optional<String> runningWorkflowUrl = orchestratorClient.provision(request);
        runningWorkflowUrl.ifPresent(s -> order.setExternalId(s.toString()));

        if (!runningWorkflowUrl.isPresent()) {
            order.setStatus(FAILURE);
        }

        return orderRepository.save(order);
    }


    protected Optional<ResourcePayload> getFasitResource(ResourceType type, String alias, VMOrderInput input) {
        ScopePayload scope = new ScopePayload();
        scope.environmentClass(input.getEnvironmentClass());
        scope.environment(input.getEnvironmentName());
        scope.application("dummy");
        scope.zone(input.getZone());
        
        return fasitRestClient.findScopedFasitResource(type, alias, scope);
    }

    protected String getWasLdapBindUserForFss(VMOrderInput input, String property) {
        String alias = "wasLdapUser";
        ScopePayload scope = new ScopePayload();
        scope.environmentClass(input.getEnvironmentClass());
        scope.environment(input.getEnvironmentName());
        scope.application("dummy");
        scope.zone(input.getZone());

        ResourcePayload credentialResource = fasitRestClient.getScopedFasitResource(ResourceType.Credential, alias, scope);

        return resolveProperty(credentialResource, property);
    }

    protected Optional<String> resolveProperty(Optional<ResourcePayload> resource, String propertyName) {
        return resource.map(resourcePayload -> resolveProperty(resourcePayload, propertyName));
    }

    protected String resolveProperty(ResourcePayload resource, String propertyName) {
        if (propertyName.equals("password")) {
			String secretRef = resource.getSecrets().get(propertyName).ref.toString();
            return fasitRestClient.getFasitSecret(secretRef);
        } else {
            return resource.getProperties().get(propertyName);
        }
    }
}