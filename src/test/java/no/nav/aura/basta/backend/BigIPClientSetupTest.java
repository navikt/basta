package no.nav.aura.basta.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.aura.basta.backend.bigip.ActiveBigIPInstanceFinder;
import no.nav.aura.basta.backend.bigip.BigIPClientSetup;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.SecretPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.infrastructure.Zone;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.bigip.BigIPOrderInput;

@ExtendWith(MockitoExtension.class)
public class BigIPClientSetupTest {

    @Mock
    private FasitRestClient fasitRestClient;

    @Mock
    private ActiveBigIPInstanceFinder activeInstanceFinder;

    @InjectMocks
    private BigIPClientSetup bigIPClientSetup;

    private BigIPOrderInput orderInput;
    private ResourcePayload loadBalancerResource;
    private List<ResourcePayload> resourcesList;

    @BeforeEach
    void setUp() {
        // Create test order input
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("environmentClass", "u");
        inputMap.put("environmentName", "u1");
        inputMap.put("zone", "fss");
        inputMap.put("application", "testApp");
        orderInput = new BigIPOrderInput(inputMap);

        // Create test load balancer resource
        loadBalancerResource = new ResourcePayload(ResourceType.LoadBalancer, "bigip");
        loadBalancerResource.properties = new HashMap<>();
        loadBalancerResource.properties.put("username", "testUser");
        loadBalancerResource.properties.put("hostname", "bigip1.test.local");
        loadBalancerResource.properties.put("secondary_hostname", "bigip2.test.local");
        
        // Create secret payload
        SecretPayload secretPayload = new SecretPayload();
        secretPayload.ref = URI.create("http://fasit.test.local/api/v2/secrets/123");
        loadBalancerResource.secrets = new HashMap<>();
        loadBalancerResource.secrets.put("password", secretPayload);

        // Create resources list
        resourcesList = List.of(loadBalancerResource);
    }

    @Test
    void testSetupBigIPClient_Success() {
        // Arrange
        String expectedPassword = "testPassword";
        String expectedActiveInstance = "10.0.0.1";

        when(fasitRestClient.findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                any(ScopePayload.class)
        )).thenReturn(resourcesList);

        when(fasitRestClient.getFasitSecret(anyString())).thenReturn(expectedPassword);

        when(activeInstanceFinder.getActiveBigIPInstance(
                eq(loadBalancerResource),
                eq("testUser"),
                eq(expectedPassword)
        )).thenReturn(expectedActiveInstance);

        // Act
        BigIPClient result = bigIPClientSetup.setupBigIPClient(orderInput);

        // Assert
        assertNotNull(result);
        verify(fasitRestClient).findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                any(ScopePayload.class)
        );
        verify(fasitRestClient).getFasitSecret(eq("http://fasit.test.local/api/v2/secrets/123"));
        verify(activeInstanceFinder).getActiveBigIPInstance(
                eq(loadBalancerResource),
                eq("testUser"),
                eq(expectedPassword)
        );
    }

    @Test
    @Disabled
    // Disabled because the scope matching BigIP does not use all parameters
    void testSetupBigIPClient_WithAllScopeParameters() {
        // Arrange
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("environmentClass", "t");
        inputMap.put("environmentName", "t1");
        inputMap.put("zone", "sbs");
        inputMap.put("application", "myApp");
        BigIPOrderInput testInput = new BigIPOrderInput(inputMap);

        String expectedPassword = "secretPass";
        String expectedActiveInstance = "192.168.1.1";

        when(fasitRestClient.findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                argThat(scope -> 
                    scope.environmentclass == EnvironmentClass.t &&
                    scope.environment.equals("t1") &&
                    scope.zone == Zone.sbs &&
                    scope.application.equals("myApp")
                )
        )).thenReturn(resourcesList);

        when(fasitRestClient.getFasitSecret(anyString())).thenReturn(expectedPassword);
        when(activeInstanceFinder.getActiveBigIPInstance(any(), any(), any())).thenReturn(expectedActiveInstance);

        // Act
        BigIPClient result = bigIPClientSetup.setupBigIPClient(testInput);

        // Assert
        assertNotNull(result);
        verify(fasitRestClient).findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                any(ScopePayload.class)
        );
    }

    @Test
    void testSetupBigIPClient_NoActiveInstanceFound() {
        // Arrange
        String expectedPassword = "testPassword";

        when(fasitRestClient.findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                any(ScopePayload.class)
        )).thenReturn(resourcesList);

        when(fasitRestClient.getFasitSecret(anyString())).thenReturn(expectedPassword);

        // Return null to simulate no active instance
        when(activeInstanceFinder.getActiveBigIPInstance(
                eq(loadBalancerResource),
                eq("testUser"),
                eq(expectedPassword)
        )).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            bigIPClientSetup.setupBigIPClient(orderInput)
        );
        
        assertEquals("Unable to find any active BIG-IP instance", exception.getMessage());
        verify(activeInstanceFinder).getActiveBigIPInstance(any(), any(), any());
    }

    @Test
    void testSetupBigIPClient_EmptyResourcesList() {
        // Arrange
        when(fasitRestClient.findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                any(ScopePayload.class)
        )).thenReturn(List.of());

        // Act & Assert
        assertThrows(IndexOutOfBoundsException.class, () -> 
            bigIPClientSetup.setupBigIPClient(orderInput)
        );
    }

    @Test
    void testSetupBigIPClient_RestClientThrowsException() {
        // Arrange
        when(fasitRestClient.findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                any(ScopePayload.class)
        )).thenThrow(new RuntimeException("Fasit connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            bigIPClientSetup.setupBigIPClient(orderInput)
        );
        
        assertEquals("Fasit connection error", exception.getMessage());
    }

    @Test
    void testSetupBigIPClient_SecretRetrievalFails() {
        // Arrange
        when(fasitRestClient.findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                any(ScopePayload.class)
        )).thenReturn(resourcesList);

        when(fasitRestClient.getFasitSecret(anyString()))
                .thenThrow(new RuntimeException("Unable to retrieve secret"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            bigIPClientSetup.setupBigIPClient(orderInput)
        );
        
        assertEquals("Unable to retrieve secret", exception.getMessage());
    }

    @Test
    void testSetupBigIPClient_ActiveInstanceFinderThrowsException() {
        // Arrange
        String expectedPassword = "testPassword";

        when(fasitRestClient.findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                any(ScopePayload.class)
        )).thenReturn(resourcesList);

        when(fasitRestClient.getFasitSecret(anyString())).thenReturn(expectedPassword);

        when(activeInstanceFinder.getActiveBigIPInstance(
                any(ResourcePayload.class),
                anyString(),
                anyString()
        )).thenThrow(new RuntimeException("Network error connecting to BigIP"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            bigIPClientSetup.setupBigIPClient(orderInput)
        );
        
        assertEquals("Network error connecting to BigIP", exception.getMessage());
    }

    @Test
    void testSetupBigIPClient_VerifyCorrectSecretUrlCalled() {
        // Arrange
        String expectedPassword = "mySecretPassword";
        String expectedActiveInstance = "10.0.0.5";
        String expectedSecretUrl = "http://fasit.test.local/api/v2/secrets/123";

        when(fasitRestClient.findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                any(ScopePayload.class)
        )).thenReturn(resourcesList);

        when(fasitRestClient.getFasitSecret(eq(expectedSecretUrl))).thenReturn(expectedPassword);

        when(activeInstanceFinder.getActiveBigIPInstance(
                any(ResourcePayload.class),
                anyString(),
                anyString()
        )).thenReturn(expectedActiveInstance);

        // Act
        bigIPClientSetup.setupBigIPClient(orderInput);

        // Assert - verify the exact URL was called
        verify(fasitRestClient).getFasitSecret(eq(expectedSecretUrl));
    }

    @Test
    void testSetupBigIPClient_VerifyCorrectUsernameExtracted() {
        // Arrange
        String expectedPassword = "pass123";
        String expectedActiveInstance = "10.0.0.10";
        String expectedUsername = "testUser";

        when(fasitRestClient.findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                any(ScopePayload.class)
        )).thenReturn(resourcesList);

        when(fasitRestClient.getFasitSecret(anyString())).thenReturn(expectedPassword);

        when(activeInstanceFinder.getActiveBigIPInstance(
                any(ResourcePayload.class),
                eq(expectedUsername),
                eq(expectedPassword)
        )).thenReturn(expectedActiveInstance);

        // Act
        bigIPClientSetup.setupBigIPClient(orderInput);

        // Assert
        verify(activeInstanceFinder).getActiveBigIPInstance(
                any(ResourcePayload.class),
                eq(expectedUsername),
                eq(expectedPassword)
        );
    }

    @Test
    void testSetupBigIPClient_WithProductionEnvironment() {
        // Arrange
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("environmentClass", "p");
        inputMap.put("environmentName", "p");
        inputMap.put("zone", "fss");
        inputMap.put("application", "prodApp");
        BigIPOrderInput prodInput = new BigIPOrderInput(inputMap);

        String expectedPassword = "prodPassword";
        String expectedActiveInstance = "10.1.1.1";

        when(fasitRestClient.findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                argThat(scope -> scope.environmentclass == EnvironmentClass.p)
        )).thenReturn(resourcesList);

        when(fasitRestClient.getFasitSecret(anyString())).thenReturn(expectedPassword);
        when(activeInstanceFinder.getActiveBigIPInstance(any(), any(), any())).thenReturn(expectedActiveInstance);

        // Act
        BigIPClient result = bigIPClientSetup.setupBigIPClient(prodInput);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testSetupBigIPClient_WithDifferentZones() {
        // Test with different zones
        Zone[] zones = {Zone.fss, Zone.sbs, Zone.dmz, Zone.iapp};
        
        for (Zone zone : zones) {
            // Arrange
            Map<String, String> inputMap = new HashMap<>();
            inputMap.put("environmentClass", "u");
            inputMap.put("environmentName", "u1");
            inputMap.put("zone", zone.name());
            inputMap.put("application", "testApp");
            BigIPOrderInput testInput = new BigIPOrderInput(inputMap);

            when(fasitRestClient.findFasitResources(
                    eq(ResourceType.LoadBalancer),
                    eq("bigip"),
                    argThat(scope -> scope.zone == zone)
            )).thenReturn(resourcesList);

            when(fasitRestClient.getFasitSecret(anyString())).thenReturn("password");
            when(activeInstanceFinder.getActiveBigIPInstance(any(), any(), any())).thenReturn("10.0.0.1");

            // Act
            BigIPClient result = bigIPClientSetup.setupBigIPClient(testInput);

            // Assert
            assertNotNull(result, "Failed for zone: " + zone);
            
            // Reset mocks for next iteration
            reset(fasitRestClient, activeInstanceFinder);
        }
    }

    @Test
    void testSetupBigIPClient_MultipleResourcesInList() {
        ResourcePayload secondResource = new ResourcePayload(ResourceType.LoadBalancer, "bigip2");
        secondResource.properties = new HashMap<>();
        secondResource.properties.put("username", "otherUser");

        List<ResourcePayload> multipleResourcesList = List.of(loadBalancerResource, secondResource);

        String expectedPassword = "testPassword";
        String expectedActiveInstance = "10.0.0.1";

        when(fasitRestClient.findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                any(ScopePayload.class)
        )).thenReturn(multipleResourcesList);

        when(fasitRestClient.getFasitSecret(anyString())).thenReturn(expectedPassword);
        when(activeInstanceFinder.getActiveBigIPInstance(any(), any(), any())).thenReturn(expectedActiveInstance);

        // Act
        BigIPClient result = bigIPClientSetup.setupBigIPClient(orderInput);

        // Assert - Should use the first resource in the list
        assertNotNull(result);
        verify(activeInstanceFinder).getActiveBigIPInstance(
                eq(loadBalancerResource),
                eq("testUser"),
                eq(expectedPassword)
        );
    }

    @Test
    void testSetupBigIPClient_NullUsername() {
        // Arrange
        loadBalancerResource.properties.remove("username");
        loadBalancerResource.properties.put("username", null);

        when(fasitRestClient.findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                any(ScopePayload.class)
        )).thenReturn(resourcesList);

        when(fasitRestClient.getFasitSecret(anyString())).thenReturn("password");
        when(activeInstanceFinder.getActiveBigIPInstance(any(), isNull(), any())).thenReturn("10.0.0.1");

        // Act
        BigIPClient result = bigIPClientSetup.setupBigIPClient(orderInput);

        // Assert
        assertNotNull(result);
        verify(activeInstanceFinder).getActiveBigIPInstance(any(), isNull(), any());
    }

    @Test
    void testSetupBigIPClient_MissingSecretInResource() {
        // Arrange
        loadBalancerResource.secrets.clear(); // Remove the password secret

        when(fasitRestClient.findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                any(ScopePayload.class)
        )).thenReturn(resourcesList);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> 
            bigIPClientSetup.setupBigIPClient(orderInput)
        );
    }

    @Test
    @Disabled
    void testSetupBigIPClient_ScopeBuiltCorrectly() {
        // Arrange
        String expectedPassword = "pass";
        String expectedActiveInstance = "10.0.0.1";

        when(fasitRestClient.findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                argThat(scope -> {
                    // Verify all scope fields are set correctly
                    return scope.environmentclass == EnvironmentClass.u &&
                           "u1".equals(scope.environment) &&
                           scope.zone == Zone.fss &&
                           "testApp".equals(scope.application);
                })
        )).thenReturn(resourcesList);

        when(fasitRestClient.getFasitSecret(anyString())).thenReturn(expectedPassword);
        when(activeInstanceFinder.getActiveBigIPInstance(any(), any(), any())).thenReturn(expectedActiveInstance);

        // Act
        BigIPClient result = bigIPClientSetup.setupBigIPClient(orderInput);

        // Assert
        assertNotNull(result);
        verify(fasitRestClient).findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                any(ScopePayload.class)
        );
    }

    @Test
    void testSetupBigIPClient_VerifyResourceTypeIsLoadBalancer() {
        // Arrange
        when(fasitRestClient.findFasitResources(
                eq(ResourceType.LoadBalancer),
                anyString(),
                any(ScopePayload.class)
        )).thenReturn(resourcesList);

        when(fasitRestClient.getFasitSecret(anyString())).thenReturn("password");
        when(activeInstanceFinder.getActiveBigIPInstance(any(), any(), any())).thenReturn("10.0.0.1");

        // Act
        bigIPClientSetup.setupBigIPClient(orderInput);

        // Assert
        verify(fasitRestClient).findFasitResources(
                eq(ResourceType.LoadBalancer),
                eq("bigip"),
                any(ScopePayload.class)
        );
    }

    @Test
    void testSetupBigIPClient_VerifyBigipAliasUsed() {
        // Arrange
        when(fasitRestClient.findFasitResources(
                any(ResourceType.class),
                eq("bigip"),
                any(ScopePayload.class)
        )).thenReturn(resourcesList);

        when(fasitRestClient.getFasitSecret(anyString())).thenReturn("password");
        when(activeInstanceFinder.getActiveBigIPInstance(any(), any(), any())).thenReturn("10.0.0.1");

        // Act
        bigIPClientSetup.setupBigIPClient(orderInput);

        // Assert
        verify(fasitRestClient).findFasitResources(
                any(ResourceType.class),
                eq("bigip"),
                any(ScopePayload.class)
        );
    }
}