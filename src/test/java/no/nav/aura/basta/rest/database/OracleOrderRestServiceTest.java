package no.nav.aura.basta.rest.database;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.OracleClient;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;
import org.jboss.resteasy.spi.BadRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static no.nav.aura.basta.domain.input.database.DBOrderInput.*;
import static no.nav.aura.basta.rest.database.OracleOrderRestService.CREATE_ORACLE_DB_JSONSCHEMA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OracleOrderRestServiceTest {

    private OracleClient oracleClient;
    private OracleOrderRestService oracleRestService;
    private FasitUpdateService fasitClient;

    @BeforeEach
    public void setUp() throws Exception {
        oracleClient = mock(OracleClient.class);
        fasitClient = mock(FasitUpdateService.class);
        oracleRestService = new OracleOrderRestService(mock(OrderRepository.class), oracleClient, fasitClient);
    }

    @Test
    public void missingRequiredPropertiesYieldsBadRequest() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            OracleOrderRestService.validateRequest(CREATE_ORACLE_DB_JSONSCHEMA, Collections.emptyMap());
        });
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
        Assertions.assertThrows(BadRequestException.class, () -> {
            oracleRestService.getOEMEndpointFromFasit("danny devito");
        });
    }

    @Test
    public void fasitIDForNonexistentResourceYieldsNotFound() {
        Assertions.assertThrows(NotFoundException.class, () -> {
            when(fasitClient.getResource(anyLong())).thenReturn(null);
            oracleRestService.getOEMEndpointFromFasit("69");
        });
    }

    @Test
    public void fasitIDForNonDataSourceResourceYieldsBadRequest() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            when(fasitClient.getResource(anyLong())).thenReturn(new ResourceElement(ResourceTypeDO.BaseUrl, "aliasForResourceWithWrongType"));
            oracleRestService.getOEMEndpointFromFasit("69");
        });
    }

    @Test
    public void providingFasitIDForDataSourceWithoutOEMEndpointYieldsBadRequest() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            final ResourceElement datasourceWithoutOEMEndpoint = new ResourceElement(ResourceTypeDO.DataSource, "aliasForResourceWithoutOEMEndpoint");
            when(fasitClient.getResource(anyLong())).thenReturn(datasourceWithoutOEMEndpoint);
            oracleRestService.getOEMEndpointFromFasit("69");
        });
    }

    @Test
    public void providingFasitIDForDatasourceWithOEMEndpointYieldsOEMEndpoint() {
        final ResourceElement dbResource = new ResourceElement(ResourceTypeDO.DataSource, "myappDB");
        final String endpoint = "/some/endpoint";
        dbResource.addProperty(new PropertyElement("oemEndpoint", endpoint));
        when(fasitClient.getResource(anyLong())).thenReturn(dbResource);

        assertEquals(endpoint, oracleRestService.getOEMEndpointFromFasit("69"), "yields matching endpoint");
    }

    @Test
    public void notFindingProvidedTemplateURIInZoneYieldsBadRequest() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            when(oracleClient.getTemplatesForZone(anyString())).thenReturn(Collections.emptyList());
            oracleRestService.verifyOEMZoneHasTemplate("someZone", "templateURI");
        });
    }

    @Test
    public void validTemplateURIPassesVerification() {
        final String templateURI = "templateURI";
        when(oracleClient.getTemplatesForZone(anyString())).thenReturn(Lists.newArrayList(ImmutableMap.of("uri", templateURI, "description", "some description of zone")));
        oracleRestService.verifyOEMZoneHasTemplate("someZone", templateURI);
    }
}