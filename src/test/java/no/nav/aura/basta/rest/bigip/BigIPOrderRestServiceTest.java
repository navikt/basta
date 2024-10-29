package no.nav.aura.basta.rest.bigip;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import no.nav.aura.basta.backend.BigIPClient;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.RestClient;
import no.nav.aura.basta.backend.bigip.BigIPClientSetup;
import no.nav.aura.basta.backend.fasit.payload.ResourcePayload;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.bigip.BigIPOrderInput;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.envconfig.client.FasitRestClient;
import org.jboss.resteasy.spi.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.endsWith;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpringUnitTestConfig.class})
public class BigIPOrderRestServiceTest {

    @Inject
    protected AuthenticationManager authenticationManager;

    private BigIPOrderRestService service;

    private RestClient restClient;
    private BigIPClientSetup bigipClientSetup;
    private FasitUpdateService fasitUpdateService;
    private OrderRepository orderRepository;
    private BigIPClient bigipClient;

    @BeforeEach
    public void setup() {
        restClient = mock(RestClient.class);
        when(restClient.get(anyString(), eq(Map.class))).thenReturn(Optional.of(new HashMap<>()));
        when(restClient.get(anyString(), eq(List.class))).thenReturn(Optional.of(Lists.newArrayList(new HashMap<>())));

        bigipClientSetup = mock(BigIPClientSetup.class);
        bigipClient = mock(BigIPClient.class);
        when(bigipClient.getVirtualServer(anyString())).thenReturn(Optional.of(new HashMap<>()));
        when(bigipClient.getRules(anyString())).thenReturn(ImmutableMap.of("items", new ArrayList<>()));
        when(bigipClient.getVersion()).thenReturn("12.1.0");
        when(bigipClientSetup.setupBigIPClient(any())).thenReturn(bigipClient);

        fasitUpdateService = mock(FasitUpdateService.class);
        when(fasitUpdateService.createOrUpdateResource(anyLong(), any(ResourcePayload.class), any(Order.class))).thenReturn(Optional.of("1234"));

        orderRepository = mock(OrderRepository.class);
        when(orderRepository.save(any(Order.class))).thenReturn(new Order(OrderType.BIGIP, OrderOperation.CREATE, new BigIPOrderInput(createBasicRequest())));

        service = new BigIPOrderRestService(orderRepository, fasitUpdateService, mock(FasitRestClient.class), restClient, bigipClientSetup);

        System.setProperty("fasit_resources_v2_url", "https://thefasitresourceapi.com");
        System.setProperty("fasit_scopedresource_v2_url", "https://thefasitscopedresourceapi.com");
        System.setProperty("fasit_environments_v2_url", "https://thefasitenvironmentsapi.com");
        System.setProperty("fasit_applications_v2_url", "https://thefasitapplicationsapi.com");
        System.setProperty("fasit_rest_api_url", "https://theoldfasitapi.com");

        login("user", "user");
    }

    private void login(String username, String password) {
        Authentication token = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Test
    public void throwsBadRequestWhenTryingToUseHostnameMatchingForCommonVS() {
        Map<String, String> request = createBasicRequest();
        request.put(BigIPOrderInput.HOSTNAME, "app-t4.adeo.no");
        request.put(BigIPOrderInput.USE_HOSTNAME_MATCHING, "true");

        assertThrows(BadRequestException.class, () -> service.createBigIpConfig(request));
    }

    @Test
    public void throwsBadRequestWhenMissingContextRoots() {
        Map<String, String> request = createBasicRequest();
        request.put(BigIPOrderInput.USE_HOSTNAME_MATCHING, "false");

        assertThrows(BadRequestException.class, () -> service.createBigIpConfig(request));
    }

    @Test
    public void creatingBigIPConfigWithContextRootsWorks() {
        Map<String, String> request = createBasicRequest();
        request.put(BigIPOrderInput.CONTEXT_ROOTS, "a,b,c");
        request.put(BigIPOrderInput.USE_HOSTNAME_MATCHING, "false");

        service.createBigIpConfig(request);
        verify(bigipClient, times(1)).createRuleOnPolicy(endsWith("_eq_auto"), anyString(), anyString(), anyMap(), anyBoolean());
        verify(bigipClient, times(1)).createRuleOnPolicy(endsWith("_sw_auto"), anyString(), anyString(), anyMap(), anyBoolean());
    }

    @Test
    public void throwsBadRequestWhenMissingHostname() {
        Map<String, String> request = createBasicRequest();
        request.put(BigIPOrderInput.USE_HOSTNAME_MATCHING, "true");

        assertThrows(BadRequestException.class, () -> service.createBigIpConfig(request));
    }

    @Test
    public void creatingBigIPConfigWithHostnameWorks() {
        Map<String, String> request = createBasicRequest();
        request.put(BigIPOrderInput.HOSTNAME, "asdf.adeo.no");
        request.put(BigIPOrderInput.USE_HOSTNAME_MATCHING, "true");

        service.createBigIpConfig(request);

        verify(bigipClient, times(1)).createRuleOnPolicy(endsWith("_hostname_auto"), anyString(), anyString(), anyMap(), anyBoolean());
    }

    @Test
    public void fasitNotUpdateableWhenMultipleResources() {
        when(restClient.get(anyString(), any())).thenReturn(Optional.of(Lists.newArrayList(new HashMap(), new HashMap())));
        assertThat("not possible to update fasit when it's multiple lbconfig resources on same scope",
                service.possibleToUpdateFasit(new BigIPOrderInput(Collections.emptyMap())), is(false));
    }

    @Test
    public void fasitUpdateableWhenNoResources() {
        when(restClient.get(anyString(), any())).thenReturn(Optional.of(new ArrayList()));
        assertThat("possible to update fasit when it's no resources on same scope",
                service.possibleToUpdateFasit(new BigIPOrderInput(Collections.emptyMap())), is(true));
    }

    @Test
    public void fasitUpdateableWhenOneResource() {
        when(restClient.get(anyString(), any())).thenReturn(Optional.of(Lists.newArrayList(new HashMap())));
        assertThat("possible to update fasit when it's one resources on same scope",
                service.possibleToUpdateFasit(new BigIPOrderInput(Collections.emptyMap())), is(true));
    }

    @Test
    public void resourceCheckReturnsTrueWhenPresent() {
        when(restClient.get(anyString(), any())).thenReturn(Optional.of(new HashMap()));
        assertThat("method returns true when fasit api returns a json-object",
                service.bigipResourceExists(new BigIPOrderInput(ImmutableMap.of("environmentClass", "u", "zone", "fss"))), is(true));
    }

    @Test
    public void resourceCheckReturnsFalseWhenExceptionIsThrown() {
        when(restClient.get(anyString(), any())).thenThrow(new RuntimeException("failed somehow"));
        assertThat("method returns false when rest client throws exception",
                service.bigipResourceExists(new BigIPOrderInput(ImmutableMap.of("environmentClass", "u", "zone", "fss"))), is(false));
    }

    @Test
    public void resourceCheckReturnsFalseWhenAbsent() {
        when(restClient.get(anyString(), any())).thenReturn(Optional.empty());
        assertThat("method returns false when value is absent",
                service.bigipResourceExists(new BigIPOrderInput(ImmutableMap.of("environmentClass", "u", "zone", "fss"))), is(false));
    }

    @Test
    public void correctlyDeterminesIfVersionRequiresPolicyDrafts() {
        assertThat(BigIPOrderRestService.usesPolicyDrafts("12.1.0"), is(true));
        assertThat(BigIPOrderRestService.usesPolicyDrafts("11.5.3"), is(false));
    }

    private Map<String, String> createBasicRequest() {
        Map<String, String> request = new HashMap<>();
        request.put(BigIPOrderInput.APPLICATION_NAME, "myapp");
        request.put(BigIPOrderInput.ENVIRONMENT_CLASS, "u");
        request.put(BigIPOrderInput.ENVIRONMENT_NAME, "myenv");
        request.put(BigIPOrderInput.VIRTUAL_SERVER, "vs1");
        request.put(BigIPOrderInput.ZONE, "fss");

        return request;
    }
}