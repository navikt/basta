package no.nav.aura.basta.rest.database;

import static no.nav.aura.basta.domain.input.database.DBOrderInput.*;
import static no.nav.aura.basta.rest.database.OracleOrderRestService.CREATE_ORACLE_DB_JSONSCHEMA;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.NotFoundException;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.OracleClient;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.PropertyElement;
import no.nav.aura.envconfig.client.rest.ResourceElement;

public class OracleOrderRestServiceTest {

    private OracleClient oracleClient;
    private OracleOrderRestService oracleRestService;
    private FasitUpdateService fasitClient;

    @Before
    public void setUp() throws Exception {
        oracleClient = mock(OracleClient.class);
        fasitClient = mock(FasitUpdateService.class);
        oracleRestService = new OracleOrderRestService(mock(OrderRepository.class), oracleClient, fasitClient);
    }

    @Test
    public void mapsNAVZonesToOEMZones() {
        assertEquals("u_t_fss", OracleOrderRestService.getOEMZoneNameFrom("u", "fss"));
        assertEquals("u_t_sbs", OracleOrderRestService.getOEMZoneNameFrom("t", "sbs"));
        assertEquals("p_fss", OracleOrderRestService.getOEMZoneNameFrom("p", "fss"));
    }

    @Test(expected = NotFoundException.class)
    public void nonexistentOEMZoneYieldsNotFound() {
        when(oracleClient.getZoneURIFrom("banan")).thenThrow(new RuntimeException("no uri found"));
        oracleRestService.verifyOEMZoneExists("banan");
    }

    @Test(expected = BadRequestException.class)
    public void missingRequiredPropertiesYieldsBadRequest() {
        OracleOrderRestService.validateRequest(CREATE_ORACLE_DB_JSONSCHEMA, Collections.emptyMap());
    }

    @Test
    public void validRequestsPassValidation() {
        Map<String, String> request = new HashMap<>();
        request.put(APPLICATION_NAME, "app");
        request.put(ENVIRONMENT_NAME, "env");
        request.put(ENVIRONMENT_CLASS, "u");
        request.put(DATABASE_NAME, "x_y");
        request.put(TEMPLATE_URI, "a.b/c");
        request.put(ZONE, "fss");
        OracleOrderRestService.validateRequest(CREATE_ORACLE_DB_JSONSCHEMA, request);
    }

    @Test(expected = BadRequestException.class)
    public void nonLongFasitIDYieldsBadRequest() {
        oracleRestService.getOEMEndpointFromFasit("danny devito");
    }

    @Test(expected = NotFoundException.class)
    public void fasitIDForNonexistentResourceYieldsNotFound() {
        when(fasitClient.getResource(anyLong())).thenReturn(null);
        oracleRestService.getOEMEndpointFromFasit("69");
    }

    @Test(expected = BadRequestException.class)
    public void fasitIDForNonDataSourceResourceYieldsBadRequest() {
        when(fasitClient.getResource(anyLong())).thenReturn(new ResourceElement(ResourceTypeDO.BaseUrl, "aliasForResourceWithWrongType"));
        oracleRestService.getOEMEndpointFromFasit("69");
    }

    @Test(expected = BadRequestException.class)
    public void providingFasitIDForDataSourceWithoutOEMEndpointYieldsBadRequest() {
        final ResourceElement datasourceWithoutOEMEndpoint = new ResourceElement(ResourceTypeDO.DataSource, "aliasForResourceWithoutOEMEndpoint");
        when(fasitClient.getResource(anyLong())).thenReturn(datasourceWithoutOEMEndpoint);
        oracleRestService.getOEMEndpointFromFasit("69");
    }

    @Test
    public void providingFasitIDForDatasourceWithOEMEndpointYieldsOEMEndpoint() {
        final ResourceElement dbResource = new ResourceElement(ResourceTypeDO.DataSource, "myappDB");
        final String endpoint = "/some/endpoint";
        dbResource.addProperty(new PropertyElement("oemEndpoint", endpoint));
        when(fasitClient.getResource(anyLong())).thenReturn(dbResource);

        assertEquals("yields matching endpoint", endpoint, oracleRestService.getOEMEndpointFromFasit("69"));
    }

    @Test(expected = BadRequestException.class)
    public void notFindingProvidedTemplateURIInZoneYieldsBadRequest() {
        when(oracleClient.getTemplatesForZone(anyString())).thenReturn(Collections.emptyList());
        oracleRestService.verifyOEMZoneHasTemplate("someZone", "templateURI");
    }

    @Test
    public void validTemplateURIPassesVerification() {
        final String templateURI = "templateURI";
        when(oracleClient.getTemplatesForZone(anyString())).thenReturn(Lists.newArrayList(ImmutableMap.of("uri", templateURI, "description", "some description of zone")));
        oracleRestService.verifyOEMZoneHasTemplate("someZone", templateURI);
    }

}