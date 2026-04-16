package no.nav.aura.basta.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.http.ContentType;
import no.nav.aura.basta.ApplicationTest;
import no.nav.aura.basta.backend.fasit.payload.LifeCycleStatus;
import no.nav.aura.basta.backend.fasit.rest.model.ApplicationPayload;
import no.nav.aura.basta.backend.fasit.rest.model.EnvironmentPayload;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.ScopePayload;
import no.nav.aura.basta.backend.fasit.rest.model.infrastructure.Zone;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.domain.input.EnvironmentClass;

@Rollback
@Transactional
public class FasitLookupServiceTest extends ApplicationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ResponseEntity<String> jsonResponse(Object obj) {
        return new ResponseEntity<>(toJson(obj), HttpStatus.OK);
    }

    @BeforeEach
    public void setupMocks() {
        // Setup mock application list
        ApplicationPayload app1 = new ApplicationPayload("testapp1", "no.nav.test", "testapp1");
        ApplicationPayload app2 = new ApplicationPayload("testapp2", "no.nav.test", "testapp2");
        ApplicationPayload[] mockApplications = { app1, app2 };

        // Setup mock environment list
        EnvironmentPayload env1 = new EnvironmentPayload("t1", EnvironmentClass.t);
        EnvironmentPayload env2 = new EnvironmentPayload("q1", EnvironmentClass.q);
        EnvironmentPayload env3 = new EnvironmentPayload("p", EnvironmentClass.p);
        EnvironmentPayload[] mockEnvironments = { env1, env2, env3 };

        // Setup mock resource list
        ScopePayload scope = new ScopePayload()
                .environmentClass(EnvironmentClass.q)
                .environment("q1")
                .application("testapp1")
                .zone(Zone.fss);

        ResourcePayload resource1 = new ResourcePayload(ResourceType.DataSource, "myDataSource");
        resource1.scope = scope;
        resource1.lifeCycleStatus = LifeCycleStatus.RUNNING;
        Map<String, String> properties = new HashMap<>();
        properties.put("url", "jdbc:oracle:thin:@dbhost:1521:db");
        properties.put("username", "testuser");
        resource1.properties = properties;

        ScopePayload scope2 = new ScopePayload()
                .environmentClass(EnvironmentClass.t)
                .environment("t1")
                .application("testapp1")
                .zone(Zone.sbs);
        ResourcePayload resource2 = new ResourcePayload(ResourceType.BaseUrl, "myBaseUrl");
        resource2.scope = scope2;
        resource2.lifeCycleStatus = LifeCycleStatus.RUNNING;
        Map<String, String> properties2 = new HashMap<>();
        properties2.put("url", "https://api.example.com");
        resource2.properties = properties2;

        ResourcePayload[] mockResources = { resource1, resource2 };

        when(mockRestTemplate.exchange(
                contains("/api/v2/applications"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
            .thenReturn(jsonResponse(mockApplications));

        when(mockRestTemplate.exchange(
                contains("/api/v2/environments"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
            .thenReturn(jsonResponse(mockEnvironments));

        when(mockRestTemplate.exchange(
                contains("/api/v2/resources"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
            .thenReturn(jsonResponse(mockResources));
    }

    @Test
    public void testGetApplications_success() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/v1/fasit/applications")
        .then()
            .log().ifError()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .header("Cache-Control", containsString("max-age=3600"))
            .body("size()", equalTo(2))
            .body("[0].name", equalTo("testapp1"))
            .body("[0].groupid", equalTo("no.nav.test"))
            .body("[0].artifactid", equalTo("testapp1"))
            .body("[1].name", equalTo("testapp2"))
            .body("[1].groupid", equalTo("no.nav.test"))
            .body("[1].artifactid", equalTo("testapp2"));
    }

    @Test
    public void testGetApplications_verifyCacheHeaders() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/v1/fasit/applications")
        .then()
            .log().ifError()
            .statusCode(200)
            .header("Cache-Control", allOf(
                containsString("max-age"),
                containsString("3600")
            ));
    }

    @Test
    public void testGetApplications_emptyList() {
        when(mockRestTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
            .thenReturn(jsonResponse(new ApplicationPayload[0]));

        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/v1/fasit/applications")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("size()", equalTo(0));
    }

    @Test
    public void testGetApplications_jsonStructure() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/v1/fasit/applications")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("[0]", hasKey("name"))
            .body("[0]", hasKey("groupid"))
            .body("[0]", hasKey("artifactid"));
    }

    @Test
    public void testGetEnvironments_success() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/v1/fasit/environments")
        .then()
            .log().all()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .header("Cache-Control", containsString("max-age=3600"))
            .body("size()", equalTo(3))
            .body("[0].name", equalTo("t1"))
            .body("[0].environmentclass", equalTo("t"))
            .body("[1].name", equalTo("q1"))
            .body("[1].environmentclass", equalTo("q"))
            .body("[2].name", equalTo("p"))
            .body("[2].environmentclass", equalTo("p"));
    }

    @Test
    public void testGetEnvironments_verifyCacheHeaders() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/v1/fasit/environments")
        .then()
            .log().ifError()
            .statusCode(200)
            .header("Cache-Control", containsString("max-age=3600"));
    }

    @Test
    public void testGetEnvironments_emptyList() {
        when(mockRestTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
            .thenReturn(jsonResponse(new EnvironmentPayload[0]));

        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/v1/fasit/environments")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("size()", equalTo(0));
    }

    @Test
    public void testGetEnvironments_jsonStructure() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/v1/fasit/environments")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("[0]", hasKey("name"))
            .body("[0]", hasKey("environmentclass"));
    }

    @Test
    public void testGetClusters_unsupported() {
        given()
            .log().ifValidationFails()
            .queryParam("environment", "u1")
        .when()
            .get("/rest/v1/fasit/clusters")
        .then()
            .log().ifError()
            .statusCode(500);
    }

    @Test
    public void testGetApplicationGroups_unsupported() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/v1/fasit/applicationgroups")
        .then()
            .log().ifError()
            .statusCode(500);
    }

    @Test
    public void testGetResources_unsupported() {
        given()
            .log().ifValidationFails()
            .queryParam("envClass", "u")
            .queryParam("environment", "u1")
            .queryParam("application", "testapp")
            .queryParam("type", "DataSource")
            .queryParam("alias", "mydb")
            .queryParam("bestmatch", "true")
            .queryParam("usage", "false")
        .when()
            .get("/rest/v1/fasit/resources")
        .then()
            .log().ifError()
            .statusCode(500);
    }

    @Test
    public void testFindResources_success() {
        given()
            .log().ifValidationFails()
            .queryParam("environmentclass", "u")
            .queryParam("environment", "u1")
            .queryParam("application", "testapp1")
            .queryParam("type", "DataSource")
            .queryParam("alias", "myDataSource")
            .queryParam("zone", "fss")
        .when()
            .get("/rest/v2/fasit/resources")
        .then()
            .log().ifError()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .header("Cache-Control", containsString("max-age=3600"))
            .body("size()", equalTo(2))
            .body("[0].type", equalTo("DataSource"))
            .body("[0].alias", equalTo("myDataSource"))
            .body("[0].properties.url", equalTo("jdbc:oracle:thin:@dbhost:1521:db"))
            .body("[0].properties.username", equalTo("testuser"))
            .body("[1].type", equalTo("BaseUrl"))
            .body("[1].alias", equalTo("myBaseUrl"))
            .body("[1].properties.url", equalTo("https://api.example.com"));
    }

    @Test
    public void testFindResources_withMinimalParams() {
        given()
            .log().ifValidationFails()
            .queryParam("environmentclass", "t")
        .when()
            .get("/rest/v2/fasit/resources")
        .then()
            .log().ifError()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", equalTo(2));
    }

    @Test
    public void testFindResources_withoutEnvironment() {
        given()
            .log().ifValidationFails()
            .queryParam("environmentclass", "u")
            .queryParam("application", "testapp1")
            .queryParam("type", "BaseUrl")
        .when()
            .get("/rest/v2/fasit/resources")
        .then()
            .log().ifError()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    public void testFindResources_withoutApplication() {
        given()
            .log().ifValidationFails()
            .queryParam("environmentclass", "p")
            .queryParam("environment", "p")
            .queryParam("type", "DataSource")
        .when()
            .get("/rest/v2/fasit/resources")
        .then()
            .log().ifError()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    public void testFindResources_missingEnvironmentClass() {
        given()
            .log().ifValidationFails()
            .queryParam("environment", "u1")
            .queryParam("application", "testapp1")
        .when()
            .get("/rest/v2/fasit/resources")
        .then()
            .log().ifError()
            .statusCode(400);
    }

    @Test
    public void testFindResources_withEnvironmnetClassAndType() {
        given()
            .log().ifValidationFails()
            .queryParam("environmentclass", "u")
            .queryParam("type", "queuemanager")
            .log().all()
        .when()
            .get("/rest/v2/fasit/resources")
        .then()
            .log().all()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    public void testFindResources_invalidEnvironmentClass() {
        given()
            .log().ifValidationFails()
            .queryParam("environmentclass", "invalid")
        .when()
            .get("/rest/v2/fasit/resources")
        .then()
            .log().ifError()
            .statusCode(500);
    }

    @Test
    public void testFindResources_emptyList() {
        when(mockRestTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
            .thenReturn(jsonResponse(new ResourcePayload[0]));

        given()
            .log().ifValidationFails()
            .queryParam("environmentclass", "u")
        .when()
            .get("/rest/v2/fasit/resources")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("size()", equalTo(0));
    }

    @Test
    public void testFindResources_verifyCacheHeaders() {
        given()
            .log().ifValidationFails()
            .queryParam("environmentclass", "u")
        .when()
            .get("/rest/v2/fasit/resources")
        .then()
            .log().ifError()
            .statusCode(200)
            .header("Cache-Control", allOf(
                containsString("max-age"),
                containsString("3600")
            ));
    }

    @Test
    public void testFindResources_allResourceTypes() {
        for (ResourceType type : new ResourceType[]{ResourceType.DataSource, ResourceType.BaseUrl,
                ResourceType.Queue, ResourceType.QueueManager}) {
            given()
                .log().ifValidationFails()
                .queryParam("environmentclass", "u")
                .queryParam("type", type.toString())
            .when()
                .get("/rest/v2/fasit/resources")
            .then()
                .log().ifError()
                .statusCode(200);
        }
    }

    @Test
    public void testFindResources_allZones() {
        for (Zone zone : new Zone[]{Zone.fss, Zone.sbs}) {
            given()
                .log().ifValidationFails()
                .queryParam("environmentclass", "u")
                .queryParam("zone", zone.toString())
            .when()
                .get("/rest/v2/fasit/resources")
            .then()
                .log().ifError()
                .statusCode(200)
                .body("size()", equalTo(2));
        }
    }

    @Test
    public void testFindResources_scopePayloadConstruction() {
        given()
            .log().ifValidationFails()
            .queryParam("environmentclass", "p")
            .queryParam("environment", "p")
            .queryParam("application", "myapp")
            .queryParam("type", "DataSource")
            .queryParam("alias", "mydb")
            .queryParam("zone", "fss")
        .when()
            .get("/rest/v2/fasit/resources")
        .then()
            .log().ifError()
            .statusCode(200);
    }

    @Test
    public void testFindResources_jsonStructure() {
        given()
            .log().ifValidationFails()
            .queryParam("environmentclass", "u")
        .when()
            .get("/rest/v2/fasit/resources")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("[0]", hasKey("type"))
            .body("[0]", hasKey("alias"))
            .body("[0]", hasKey("scope"))
            .body("[0]", hasKey("properties"));
    }

    @Test
    public void testFindResources_verifyLifecycleStatus() {
        given()
            .log().ifValidationFails()
            .queryParam("environmentclass", "u")
            .queryParam("environment", "u1")
        .when()
            .get("/rest/v2/fasit/resources")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("[0].lifecyclestatus", equalTo("RUNNING"));
    }

    @Test
    public void testFindResources_allEnvironmentClasses() {
        for (EnvironmentClass envClass : new EnvironmentClass[]{EnvironmentClass.u, EnvironmentClass.t,
                EnvironmentClass.q, EnvironmentClass.p}) {
            given()
                .log().ifValidationFails()
                .queryParam("environmentclass", envClass.toString())
            .when()
                .get("/rest/v2/fasit/resources")
            .then()
                .log().all()
                .statusCode(200);
        }
    }

    // ========== COMPREHENSIVE VALIDATION EXAMPLES ==========

    @Test
    public void testFindResources_validateAllElements_technique1() {
        given()
            .log().ifValidationFails()
            .queryParam("environmentclass", "u")
        .when()
            .get("/rest/v2/fasit/resources")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("type", everyItem(notNullValue()))
            .body("alias", everyItem(notNullValue()))
            .body("scope", everyItem(notNullValue()))
            .body("properties", everyItem(notNullValue()))
            .body("lifecyclestatus", everyItem(equalTo("RUNNING")));
    }

    @Test
    public void testFindResources_validateByIndex_technique2() {
        given()
            .log().ifValidationFails()
            .queryParam("environmentclass", "u")
        .when()
            .get("/rest/v2/fasit/resources")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("[0].type", equalTo("DataSource"))
            .body("[0].alias", equalTo("myDataSource"))
            .body("[0].scope.environment", equalTo("q1"))
            .body("[0].scope.application", equalTo("testapp1"))
            .body("[0].scope.zone", equalTo("fss"))
            .body("[0].properties.url", equalTo("jdbc:oracle:thin:@dbhost:1521:db"))
            .body("[0].properties.username", equalTo("testuser"))
            .body("[0].lifecyclestatus", equalTo("RUNNING"))
            .body("[1].type", equalTo("BaseUrl"))
            .body("[1].alias", equalTo("myBaseUrl"))
            .body("[1].scope.environmentclass", equalTo("t"))
            .body("[1].scope.zone", equalTo("sbs"))
            .body("[1].properties.url", equalTo("https://api.example.com"))
            .body("[1].lifecyclestatus", equalTo("RUNNING"));
    }

    @Test
    public void testFindResources_validateWithFind_technique3() {
        given()
            .log().ifValidationFails()
            .queryParam("environmentclass", "u")
        .when()
            .get("/rest/v2/fasit/resources")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("find { it.type == 'DataSource' }.alias", equalTo("myDataSource"))
            .body("find { it.type == 'DataSource' }.properties.url",
                equalTo("jdbc:oracle:thin:@dbhost:1521:db"))
            .body("find { it.type == 'DataSource' }.properties.username", equalTo("testuser"))
            .body("find { it.type == 'BaseUrl' }.alias", equalTo("myBaseUrl"))
            .body("find { it.type == 'BaseUrl' }.properties.url",
                equalTo("https://api.example.com"));
    }

    @Test
    public void testFindResources_validateWithCollections_technique4() {
        given()
            .log().ifValidationFails()
            .queryParam("environmentclass", "u")
        .when()
            .get("/rest/v2/fasit/resources")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("type", hasItems("DataSource", "BaseUrl"))
            .body("alias", hasItems("myDataSource", "myBaseUrl"))
            .body("properties.url", hasItem(containsString("jdbc:")))
            .body("properties.url", hasItem(containsString("https://")));
    }

    @Test
    public void testGetApplications_validateAllElements_technique5() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/v1/fasit/applications")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("name", everyItem(notNullValue()))
            .body("groupid", everyItem(notNullValue()))
            .body("artifactid", everyItem(notNullValue()))
            .body("name", hasItems("testapp1", "testapp2"))
            .body("groupid", everyItem(equalTo("no.nav.test")))
            .body("[0].name", equalTo("testapp1"))
            .body("[0].groupid", equalTo("no.nav.test"))
            .body("[0].artifactid", equalTo("testapp1"));
    }

    @Test
    public void testGetEnvironments_validateAllElements_technique6() {
        given()
            .log().ifValidationFails()
        .when()
            .get("/rest/v1/fasit/environments")
        .then()
            .log().ifError()
            .statusCode(200)
            .body("size()", equalTo(3))
            .body("name", everyItem(notNullValue()))
            .body("environmentclass", everyItem(notNullValue()))
            .body("find { it.name == 't1' }.environmentclass", equalTo("t"))
            .body("find { it.name == 'q1' }.environmentclass", equalTo("q"))
            .body("find { it.name == 'p' }.environmentclass", equalTo("p"))
            .body("name", hasItems("t1", "q1", "p"))
            .body("environmentclass", hasItems("t", "q", "p"));
    }
}
