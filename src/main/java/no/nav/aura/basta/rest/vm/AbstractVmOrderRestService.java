package no.nav.aura.basta.rest.vm;

import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.fasit.payload.ResourcePayload;
import no.nav.aura.basta.backend.fasit.payload.ResourceType;
import no.nav.aura.basta.backend.fasit.payload.ScopePayload;
import no.nav.aura.basta.backend.fasit.payload.Zone;
import no.nav.aura.basta.backend.vmware.orchestrator.OrchestratorClient;
import no.nav.aura.basta.backend.vmware.orchestrator.request.OrchestatorRequest;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.repository.OrderRepository;

import java.util.Optional;

import static no.nav.aura.basta.domain.input.vm.OrderStatus.FAILURE;

public abstract class AbstractVmOrderRestService {
    protected RestClient fasitRestClient;
    protected OrderRepository orderRepository;
    protected OrchestratorClient orchestratorClient;

    public AbstractVmOrderRestService() {
    }

    public AbstractVmOrderRestService(OrderRepository orderRepository, OrchestratorClient orchestratorClient) {
        this.orderRepository = orderRepository;
        this.orchestratorClient = orchestratorClient;
    }

    public AbstractVmOrderRestService(OrderRepository orderRepository, OrchestratorClient orchestratorClient, RestClient fasitRestClient) {
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
        ScopePayload scope = new ScopePayload(input.getEnvironmentClass().name())
                .zone(input.getZone())
                .environment(input.getEnvironmentName())
                .application("basta");
        return fasitRestClient.findScopedFasitResource(type, alias, scope);
    }

    protected String getWasLdapBindUserForFss(VMOrderInput input, String property) {
        String alias = "wasLdapUser";
        ScopePayload scope = new ScopePayload(input.getEnvironmentClass().name())
                .environment(input.getEnvironmentName())
                .zone(Zone.fss)
                .application("basta");

        ResourcePayload credentialResource = fasitRestClient.getScopedFasitResource(ResourceType.credential, alias, scope);

        return resolveProperty(credentialResource, property);
    }

    protected Optional<String> resolveProperty(Optional<ResourcePayload> resource, String propertyName) {
        return resource.map(resourcePayload -> resolveProperty(resourcePayload, propertyName));
    }

    protected String resolveProperty(ResourcePayload resource, String propertyName) {
        if (propertyName.equals("password")) {
            String secretRef = resource.getSecretRef(propertyName);
            return fasitRestClient.getFasitSecret(secretRef);
        } else {
            return resource.getProperty(propertyName);
        }
    }
}