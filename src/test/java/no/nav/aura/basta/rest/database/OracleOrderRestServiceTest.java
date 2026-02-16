package no.nav.aura.basta.rest.database;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.OracleClient;
import no.nav.aura.basta.backend.fasit.rest.model.ResourcePayload;
import no.nav.aura.basta.backend.fasit.rest.model.resource.ResourceType;
import no.nav.aura.basta.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static no.nav.aura.basta.domain.input.database.DBOrderInput.*;
import static no.nav.aura.basta.rest.database.OracleOrderRestService.CREATE_ORACLE_DB_JSONSCHEMA;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OracleOrderRestServiceTest {

    private OracleClient oracleClient;
    private OracleOrderRestService oracleRestService;
    private FasitUpdateService fasitUpdateService;

    @BeforeEach
    public void setUp() throws Exception {
        oracleClient = mock(OracleClient.class);
        fasitUpdateService = mock(FasitUpdateService.class);
        oracleRestService = new OracleOrderRestService(mock(OrderRepository.class), oracleClient, fasitUpdateService);
    }

    @Test
    public void missingRequiredPropertiesYieldsBadRequest() {
        assertThrows(ResponseStatusException.class, () ->
            OracleOrderRestService.validateRequest(CREATE_ORACLE_DB_JSONSCHEMA, Collections.emptyMap()));
    }

    @Test
    public void validRequestsPassValidation() {
        Map<String, String> request = new HashMap<>();
        request.put(APPLICATION_NAME, "app");
        request.put(ENVIRONMENT_NAME, "env");
        request.put(ENVIRONMENT_CLASS, "u");
        request.put(DATABASE_NAME, "x_y");
        request.put(TEMPLATE_URI, "a.b/c");
        request.put(ZONE_URI, "b.c/d");
        OracleOrderRestService.validateRequest(CREATE_ORACLE_DB_JSONSCHEMA, request);
    }

    @Test
    public void nonLongFasitIDYieldsBadRequest() {
        assertThrows(ResponseStatusException.class, () -> oracleRestService.getOEMEndpointFromFasit("danny devito"));
    }

    @Test
    public void fasitIDForNonexistentResourceYieldsNotFound() {
        when(fasitUpdateService.getResource(anyLong())).thenReturn(null);
        assertThrows(ResponseStatusException.class, () -> oracleRestService.getOEMEndpointFromFasit("69"));
    }

    @Test
    public void fasitIDForNonDataSourceResourceYieldsBadRequest() {
        when(fasitUpdateService.getResource(anyLong())).thenReturn(Optional.of(new ResourcePayload(ResourceType.BaseUrl, "aliasForResourceWithWrongType")));
        assertThrows(ResponseStatusException.class, () -> oracleRestService.getOEMEndpointFromFasit("69"));
    }

    @Test
    public void providingFasitIDForDataSourceWithoutOEMEndpointYieldsBadRequest() {
        final ResourcePayload datasourceWithoutOEMEndpoint = new ResourcePayload(ResourceType.DataSource, "aliasForResourceWithoutOEMEndpoint");
        when(fasitUpdateService.getResource(anyLong())).thenReturn(Optional.of(datasourceWithoutOEMEndpoint));
        assertThrows(ResponseStatusException.class, () -> oracleRestService.getOEMEndpointFromFasit("69"));
    }

    @Test
    public void providingFasitIDForDatasourceWithOEMEndpointYieldsOEMEndpoint() {
        final ResourcePayload dbResource = new ResourcePayload(ResourceType.DataSource, "myappDB");
        final String endpoint = "/some/endpoint";
        dbResource.addProperty("oemEndpoint", endpoint);
        when(fasitUpdateService.getResource(anyLong())).thenReturn(Optional.of(dbResource));

        assertEquals("yields matching endpoint", endpoint, oracleRestService.getOEMEndpointFromFasit("69"));
    }

    @Test
    public void notFindingProvidedTemplateURIInZoneYieldsBadRequest() {
        when(oracleClient.getTemplatesForZone(anyString())).thenReturn(Collections.emptyList());
        assertThrows(ResponseStatusException.class, () -> oracleRestService.verifyOEMZoneHasTemplate("someZone", "templateURI"));
    }

    @Test
    public void validTemplateURIPassesVerification() {
        final String templateURI = "templateURI";
        when(oracleClient.getTemplatesForZone(anyString())).thenReturn(Lists.newArrayList(ImmutableMap.of("uri", templateURI, "description", "some description of zone")));
        oracleRestService.verifyOEMZoneHasTemplate("someZone", templateURI);
    }
}