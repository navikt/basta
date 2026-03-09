package no.nav.aura.basta.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import no.nav.aura.basta.backend.fasit.rest.model.ApplicationListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ApplicationPayload;
import no.nav.aura.basta.backend.fasit.rest.model.EnvironmentListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.EnvironmentPayload;
import no.nav.aura.basta.backend.fasit.rest.model.FasitSearchResults;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcesListPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.SearchResultPayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.domain.input.EnvironmentClass;

@ExtendWith(MockitoExtension.class)
public class FasitRestClientTest {

    @Mock
    private RestTemplate restTemplate;

    private FasitRestClient fasitRestClient;

    private static final String FASIT_BASE_URL = "http://test-fasit.example.com";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";

    @BeforeEach
    void setUp() {
        // Create a real instance of FasitRestClient with test credentials
        fasitRestClient = new FasitRestClient(FASIT_BASE_URL, USERNAME, PASSWORD);
        // Inject the mocked RestTemplate into the real FasitRestClient instance
        ReflectionTestUtils.setField(fasitRestClient, "restTemplate", restTemplate);
    }

    @Test
    void testConstructorWithUsernameAndPassword() {
        RestClient client = new RestClient("user", "pass");
        assertNotNull(client);
    }

    @Test
    void testGetScopedFasitResource_Success() {
        // Arrange
        ResourceType type = ResourceType.DataSource;
        String alias = "testAlias";
        ScopePayload scope = new ScopePayload()
                .environmentClass(EnvironmentClass.u)
                .environment("u1")
                .application("testApp");

        ResourcePayload expectedResource = new ResourcePayload(type, alias);
        ResponseEntity<ResourcePayload> response = new ResponseEntity<>(expectedResource, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ResourcePayload.class)
        )).thenReturn(response);

        // Act
        ResourcePayload result = fasitRestClient.getScopedFasitResource(type, alias, scope);

        // Assert
        assertNotNull(result);
        assertEquals(alias, result.alias);
        assertEquals(type, result.type);
    }

    @Test
    void testGetScopedFasitResource_NotFound() {
        // Arrange
        ResourceType type = ResourceType.DataSource;
        String alias = "testAlias";
        ScopePayload scope = new ScopePayload()
                .environmentClass(EnvironmentClass.u)
                .environment("u1");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ResourcePayload.class)
        )).thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "", HttpHeaders.EMPTY, null, null));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            fasitRestClient.getScopedFasitResource(type, alias, scope)
        );
    }

    @Test
    void testFindScopedFasitResource_Success() {
        // Arrange
        ResourceType type = ResourceType.DataSource;
        String alias = "testAlias";
        ScopePayload scope = new ScopePayload()
                .environmentClass(EnvironmentClass.u)
                .environment("u1");

        ResourcePayload expectedResource = new ResourcePayload(type, alias);
        ResponseEntity<ResourcePayload> response = new ResponseEntity<>(expectedResource, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ResourcePayload.class)
        )).thenReturn(response);

        // Act
        Optional<ResourcePayload> result = fasitRestClient.findScopedFasitResource(type, alias, scope);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(alias, result.get().alias);
    }

    @Test
    void testFindScopedFasitResource_NotFound() {
        // Arrange
        ResourceType type = ResourceType.DataSource;
        String alias = "testAlias";
        ScopePayload scope = new ScopePayload()
                .environmentClass(EnvironmentClass.u)
                .environment("u1");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ResourcePayload.class)
        )).thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "", HttpHeaders.EMPTY, null, null));

        // Act
        Optional<ResourcePayload> result = fasitRestClient.findScopedFasitResource(type, alias, scope);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testGetFasitResourceById_Success() {
        // Arrange
        long id = 123L;
        ResourcePayload expectedResource = new ResourcePayload(ResourceType.DataSource, "alias");
        ResponseEntity<ResourcePayload> response = new ResponseEntity<>(expectedResource, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ResourcePayload.class)
        )).thenReturn(response);

        // Act
        Optional<ResourcePayload> result = fasitRestClient.getFasitResourceById(id);

        // Assert
        assertTrue(result.isPresent());
    }

    @Test
    void testGetNodeCountFor() {
        // Arrange
        String environment = "u1";
        String application = "testApp";
        HttpHeaders headers = new HttpHeaders();
        headers.add("total_count", "5");
        ResponseEntity<String> response = new ResponseEntity<>("", headers, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // Act
        Integer count = fasitRestClient.getNodeCountFor(environment, application);

        // Assert
        assertEquals(5, count);
    }

    @Test
    void testGetCount_Success() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/nodes";
        HttpHeaders headers = new HttpHeaders();
        headers.add("total_count", "10");
        ResponseEntity<String> response = new ResponseEntity<>("", headers, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // Act
        Integer count = fasitRestClient.getCount(url);

        // Assert
        assertEquals(10, count);
    }

    @Test
    void testGetCount_MissingHeader() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/nodes";
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> fasitRestClient.getCount(url));
    }

    @Test
    void testSearchFasit() {
        // Arrange
        String searchQuery = "test";
        String type = "resource";
        
        SearchResultPayload searchResult = new SearchResultPayload();
        searchResult.type = type;
        searchResult.link = URI.create(FASIT_BASE_URL + "/api/v2/resources/1");
        
        List<SearchResultPayload> searchResults = Collections.singletonList(searchResult);
        FasitSearchResults fasitSearchResults = new FasitSearchResults(searchResults);

        ResourcePayload resourcePayload = new ResourcePayload(ResourceType.DataSource, "alias");
        ResponseEntity<ResourcePayload> resourceResponse = new ResponseEntity<>(resourcePayload, HttpStatus.OK);

        when(restTemplate.exchange(
                contains("/api/v1/search"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(FasitSearchResults.class)
        )).thenReturn(new ResponseEntity<>(fasitSearchResults, HttpStatus.OK));

        when(restTemplate.exchange(
                contains("/api/v2/resources"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ResourcePayload.class)
        )).thenReturn(resourceResponse);

        // Act
        List<ResourcePayload> results = fasitRestClient.searchFasit(searchQuery, type, ResourcePayload.class);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void testFindFasitResources() {
        // Arrange
        ResourceType type = ResourceType.DataSource;
        String alias = "testAlias";
        ScopePayload searchScope = new ScopePayload()
                .environmentClass(EnvironmentClass.u)
                .environment("u1")
                .application("testApp");

        List<ResourcePayload> resourceList = Collections.singletonList(
                new ResourcePayload(type, alias)
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ResourcesListPayload.class)
        )).thenReturn(new ResponseEntity<>(new ResourcesListPayload(resourceList), HttpStatus.OK));

        // Act
        ResourcesListPayload result = fasitRestClient.findFasitResources(type, alias, searchScope);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testExistsInFasit_True() {
        // Arrange
        ResourceType type = ResourceType.DataSource;
        String alias = "testAlias";
        ScopePayload searchScope = new ScopePayload()
                .environmentClass(EnvironmentClass.u);

        List<ResourcePayload> resourceList = Collections.singletonList(
                new ResourcePayload(type, alias)
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ResourcesListPayload.class)
        )).thenReturn(new ResponseEntity<>(new ResourcesListPayload(resourceList), HttpStatus.OK));

        // Act
        boolean exists = fasitRestClient.existsInFasit(type, alias, searchScope);

        // Assert
        assertTrue(exists);
    }

    @Test
    void testExistsInFasit_False() {
        // Arrange
        ResourceType type = ResourceType.DataSource;
        String alias = "testAlias";
        ScopePayload searchScope = new ScopePayload()
                .environmentClass(EnvironmentClass.u);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ResourcesListPayload.class)
        )).thenReturn(new ResponseEntity<>(new ResourcesListPayload(Collections.emptyList()), HttpStatus.OK));

        // Act
        boolean exists = fasitRestClient.existsInFasit(type, alias, searchScope);

        // Assert
        assertFalse(exists);
    }

    @Test
    void testGetFasitSecret() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/secrets/123";
        String secretValue = "secretPassword";
        ResponseEntity<String> response = new ResponseEntity<>(secretValue, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // Act
        String result = fasitRestClient.getFasitSecret(url);

        // Assert
        assertEquals(secretValue, result);
    }

    @Test
    void testGetFasitSecret_NotFound() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/secrets/123";

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "", HttpHeaders.EMPTY, null, null));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> fasitRestClient.getFasitSecret(url));
    }

    @Test
    void testGet_Success() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources/123";
        ResourcePayload expectedResource = new ResourcePayload(ResourceType.DataSource, "alias");
        ResponseEntity<ResourcePayload> response = new ResponseEntity<>(expectedResource, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ResourcePayload.class)
        )).thenReturn(response);

        // Act
        Optional<ResourcePayload> result = fasitRestClient.get(url, ResourcePayload.class);

        // Assert
        assertTrue(result.isPresent());
    }

    @Test
    void testGet_NotFound() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources/123";

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ResourcePayload.class)
        )).thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "", HttpHeaders.EMPTY, null, null));

        // Act
        Optional<ResourcePayload> result = fasitRestClient.get(url, ResourcePayload.class);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testDelete_Success() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources/123";
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // Act
        ResponseEntity<String> result = fasitRestClient.delete(url);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void testDeleteFasitResource_Success() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources/123";
        String onBehalfOfUser = "admin";
        String comment = "test delete";
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // Act
        ResponseEntity<String> result = fasitRestClient.deleteFasitResource(url, onBehalfOfUser, comment);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        
        // Verify headers were set
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.DELETE), captor.capture(), eq(String.class));
        HttpHeaders headers = captor.getValue().getHeaders();
        assertEquals(onBehalfOfUser, headers.getFirst("x-onbehalfof"));
        assertEquals(comment, headers.getFirst("x-comment"));
    }

    @Test
    void testCheckResponseAndThrowException_Forbidden() {
        // Arrange
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.FORBIDDEN);
        String url = FASIT_BASE_URL + "/api/v2/resources";

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () -> 
            fasitRestClient.checkResponseAndThrowException(response, url)
        );
        assertTrue(exception.getMessage().contains("Access forbidden"));
    }

    @Test
    void testCheckResponseAndThrowException_Unauthorized() {
        // Arrange
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        String url = FASIT_BASE_URL + "/api/v2/resources";

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () -> 
            fasitRestClient.checkResponseAndThrowException(response, url)
        );
        assertTrue(exception.getMessage().contains("Unauthorized"));
    }

    @Test
    void testCheckResponseAndThrowException_NotFound() {
        // Arrange
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        String url = FASIT_BASE_URL + "/api/v2/resources";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            fasitRestClient.checkResponseAndThrowException(response, url)
        );
        assertTrue(exception.getMessage().contains("Not found"));
    }

    @Test
    void testCheckResponseAndThrowException_ServerError() {
        // Arrange
        ResponseEntity<String> response = new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);
        String url = FASIT_BASE_URL + "/api/v2/resources";

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            fasitRestClient.checkResponseAndThrowException(response, url)
        );
        assertTrue(exception.getMessage().contains("Error calling"));
    }

    @Test
    void testCreateFasitResource_Success() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources";
        String payload = "{\"type\":\"DataSource\"}";
        String onBehalfOfUser = "admin";
        String comment = "test create";
        
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Location", FASIT_BASE_URL + "/api/v2/resources/123");
        ResponseEntity<String> response = new ResponseEntity<>("", responseHeaders, HttpStatus.CREATED);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // Act
        Optional<String> result = fasitRestClient.createFasitResource(url, payload, onBehalfOfUser, comment);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("123", result.get());
    }

    @Test
    void testCreateFasitResource_NoLocationHeader() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources";
        String payload = "{\"type\":\"DataSource\"}";
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.CREATED);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // Act
        Optional<String> result = fasitRestClient.createFasitResource(url, payload, null, null);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testPost_Success() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources";
        String payload = "{\"type\":\"DataSource\"}";
        ResponseEntity<String> response = new ResponseEntity<>("Success", HttpStatus.OK);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // Act
        ResponseEntity<String> result = fasitRestClient.post(url, payload);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Success", result.getBody());
    }

    @Test
    void testUpdateFasitResourceAndReturnResourcePayload_Success() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources/123";
        String payload = "{\"alias\":\"updatedAlias\"}";
        String onBehalfOfUser = "admin";
        String comment = "update resource";
        
        ResourcePayload updatedResource = new ResourcePayload(ResourceType.DataSource, "updatedAlias");
        ResponseEntity<ResourcePayload> response = new ResponseEntity<>(updatedResource, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(ResourcePayload.class)
        )).thenReturn(response);

        // Act
        ResourcePayload result = fasitRestClient.updateFasitResourceAndReturnResourcePayload(url, payload, onBehalfOfUser, comment);

        // Assert
        assertNotNull(result);
        assertEquals("updatedAlias", result.alias);
    }

    @Test
    void testUpdateFasitResource_Success() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources/123";
        String payload = "{\"alias\":\"updatedAlias\"}";
        String onBehalfOfUser = "admin";
        String comment = "update resource";
        
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Location", FASIT_BASE_URL + "/api/v2/resources/123");
        ResponseEntity<String> response = new ResponseEntity<>("", responseHeaders, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // Act
        Optional<String> result = fasitRestClient.updateFasitResource(url, payload, onBehalfOfUser, comment);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("123", result.get());
    }

    @Test
    void testPut_Success() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources/123";
        String payload = "{\"alias\":\"updatedAlias\"}";
        ResponseEntity<String> response = new ResponseEntity<>("Success", HttpStatus.OK);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // Act
        ResponseEntity<String> result = fasitRestClient.put(url, payload);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void testPatch_Success() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources/123";
        String payload = "{\"alias\":\"patchedAlias\"}";
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // Act
        assertDoesNotThrow(() -> fasitRestClient.patch(url, payload));

        // Assert
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testGetApplicationByName_Success() {
        // Arrange
        String applicationName = "testApp";
        ApplicationPayload expectedApp = new ApplicationPayload();
        expectedApp.name = applicationName;
        ResponseEntity<ApplicationPayload> response = new ResponseEntity<>(expectedApp, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ApplicationPayload.class)
        )).thenReturn(response);

        // Act
        ApplicationPayload result = fasitRestClient.getApplicationByName(applicationName);

        // Assert
        assertNotNull(result);
        assertEquals(applicationName, result.name);
    }

    @Test
    void testGetApplicationByName_NotFound() {
        // Arrange
        String applicationName = "testApp";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ApplicationPayload.class)
        )).thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "", HttpHeaders.EMPTY, null, null));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            fasitRestClient.getApplicationByName(applicationName)
        );
    }

    @Test
    void testGetAllApplications() {
        // Arrange
        List<ApplicationPayload> appList = new ArrayList<>();
        ApplicationPayload app1 = new ApplicationPayload();
        app1.name = "app1";
        appList.add(app1);
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ApplicationListPayload.class)
        )).thenReturn(new ResponseEntity<>(new ApplicationListPayload(appList), HttpStatus.OK));

        // Act
        ApplicationListPayload result = fasitRestClient.getAllApplications();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetEnvironmentByName_Success() {
        // Arrange
        String environmentName = "u1";
        EnvironmentPayload expectedEnv = new EnvironmentPayload();
        expectedEnv.name = environmentName;
        ResponseEntity<EnvironmentPayload> response = new ResponseEntity<>(expectedEnv, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EnvironmentPayload.class)
        )).thenReturn(response);

        // Act
        EnvironmentPayload result = fasitRestClient.getEnvironmentByName(environmentName);

        // Assert
        assertNotNull(result);
        assertEquals(environmentName, result.name);
    }

    @Test
    void testGetEnvironmentByName_NotFound() {
        // Arrange
        String environmentName = "u1";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EnvironmentPayload.class)
        )).thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "", HttpHeaders.EMPTY, null, null));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            fasitRestClient.getEnvironmentByName(environmentName)
        );
    }

    @Test
    void testGetAllEnvironments() {
        // Arrange
        List<EnvironmentPayload> envList = new ArrayList<>();
        EnvironmentPayload env1 = new EnvironmentPayload();
        env1.name = "u1";
        envList.add(env1);
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EnvironmentListPayload.class)
        )).thenReturn(new ResponseEntity<>(new EnvironmentListPayload(envList), HttpStatus.OK));

        // Act
        EnvironmentListPayload result = fasitRestClient.getAllEnvironments();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testDelete_NotFoundStatus_DoesNotThrow() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources/123";
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // Act
        ResponseEntity<String> result = fasitRestClient.delete(url);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void testDeleteFasitResource_NotFoundStatus_DoesNotThrow() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources/123";
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // Act
        ResponseEntity<String> result = fasitRestClient.deleteFasitResource(url, null, null);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void testCreateFasitResource_WithException() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources";
        String payload = "{\"type\":\"DataSource\"}";

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            fasitRestClient.createFasitResource(url, payload, null, null)
        );
        assertTrue(exception.getMessage().contains("Error trying to POST"));
    }

    @Test
    void testPost_WithException() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources";
        String payload = "{\"type\":\"DataSource\"}";

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            fasitRestClient.post(url, payload)
        );
        assertTrue(exception.getMessage().contains("Error trying to POST"));
    }

    @Test
    void testUpdateFasitResourceAndReturnResourcePayload_WithException() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources/123";
        String payload = "{\"alias\":\"updatedAlias\"}";

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(ResourcePayload.class)
        )).thenThrow(new RuntimeException("Connection error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            fasitRestClient.updateFasitResourceAndReturnResourcePayload(url, payload, null, null)
        );
    }

    @Test
    void testUpdateFasitResource_WithException() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources/123";
        String payload = "{\"alias\":\"updatedAlias\"}";

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Connection error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            fasitRestClient.updateFasitResource(url, payload, null, null)
        );
    }

    @Test
    void testPut_WithException() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources/123";
        String payload = "{\"alias\":\"updatedAlias\"}";

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Connection error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            fasitRestClient.put(url, payload)
        );
    }

    @Test
    void testPatch_WithException() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources/123";
        String payload = "{\"alias\":\"patchedAlias\"}";

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Connection error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            fasitRestClient.patch(url, payload)
        );
    }

    @Test
    void testDelete_WithException() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources/123";

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Connection error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            fasitRestClient.delete(url)
        );
    }

    @Test
    void testDeleteFasitResource_WithException() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/resources/123";

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Connection error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            fasitRestClient.deleteFasitResource(url, null, null)
        );
    }

    @Test
    void testGetCount_WithHttpClientErrorException() {
        // Arrange
        String url = FASIT_BASE_URL + "/api/v2/nodes";

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(HttpClientErrorException.BadRequest.create(HttpStatus.BAD_REQUEST, "", HttpHeaders.EMPTY, null, null));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            fasitRestClient.getCount(url)
        );
        assertTrue(exception.getMessage().contains("Error getting count"));
    }

    @Test
    void testCheckResponseAndThrowException_BadRequest() {
        // Arrange
        ResponseEntity<String> response = new ResponseEntity<>("Bad request", HttpStatus.BAD_REQUEST);
        String url = FASIT_BASE_URL + "/api/v2/resources";

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            fasitRestClient.checkResponseAndThrowException(response, url)
        );
        assertTrue(exception.getMessage().contains("Error calling"));
        assertTrue(exception.getMessage().contains("400"));
    }

    @Test
    void testCheckResponseAndThrowException_WithResponseBody() {
        // Arrange
        ResponseEntity<String> response = new ResponseEntity<>("Detailed error message", HttpStatus.BAD_REQUEST);
        String url = FASIT_BASE_URL + "/api/v2/resources";

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            fasitRestClient.checkResponseAndThrowException(response, url)
        );
        assertTrue(exception.getMessage().contains("Detailed error message"));
    }

    @Test
    void testSearchFasit_EmptyResults() {
        // Arrange
        String searchQuery = "test";
        String type = "resource";
        
        List<SearchResultPayload> searchResults = Collections.emptyList();

        when(restTemplate.exchange(
                contains("/api/v1/search"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(FasitSearchResults.class)
        )).thenReturn(new ResponseEntity<>(new FasitSearchResults(searchResults), HttpStatus.OK));

        // Act
        List<ResourcePayload> results = fasitRestClient.searchFasit(searchQuery, type, ResourcePayload.class);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchFasit_FilterByType() {
        // Arrange
        String searchQuery = "test";
        String type = "resource";
        
        SearchResultPayload searchResult1 = new SearchResultPayload();
        searchResult1.type = type;
        searchResult1.link = URI.create(FASIT_BASE_URL + "/api/v2/resources/1");
        
        SearchResultPayload searchResult2 = new SearchResultPayload();
        searchResult2.type = "application";  // Different type, should be filtered out
        searchResult2.link = URI.create(FASIT_BASE_URL + "/api/v2/applications/1");
        
        List<SearchResultPayload> searchResults = List.of(searchResult1, searchResult2);

        ResourcePayload resourcePayload = new ResourcePayload(ResourceType.DataSource, "alias");
        ResponseEntity<ResourcePayload> resourceResponse = new ResponseEntity<>(resourcePayload, HttpStatus.OK);

        when(restTemplate.exchange(
                contains("/api/v1/search"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(FasitSearchResults.class)
    		)).thenReturn(new ResponseEntity<>(new FasitSearchResults(searchResults), HttpStatus.OK));

        when(restTemplate.exchange(
                contains("/api/v2/resources"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ResourcePayload.class)
        )).thenReturn(resourceResponse);

        // Act
        List<ResourcePayload> results = fasitRestClient.searchFasit(searchQuery, type, ResourcePayload.class);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());  // Only one should match the type filter
    }

    @Test
    void testGetAllApplications_Empty() {
        // Arrange
        List<ApplicationPayload> appList = Collections.emptyList();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ApplicationListPayload.class)
        )).thenReturn(new ResponseEntity<>(new ApplicationListPayload(appList), HttpStatus.OK));

        // Act
        ApplicationListPayload result = fasitRestClient.getAllApplications();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllEnvironments_Empty() {
        // Arrange
        List<EnvironmentPayload> envList = Collections.emptyList();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EnvironmentListPayload.class)
        )).thenReturn(new ResponseEntity<>(new EnvironmentListPayload(envList), HttpStatus.OK));

        // Act
        EnvironmentListPayload result = fasitRestClient.getAllEnvironments();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
