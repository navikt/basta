package no.nav.aura.basta.rest.bigip;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.*;

import javax.inject.Inject;

import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.BadRequestException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import no.nav.aura.basta.backend.BigIPClient;
import no.nav.aura.basta.backend.FasitUpdateService;
import no.nav.aura.basta.backend.bigip.BigIPClientSetup;
import no.nav.aura.basta.backend.bigip.RestClient;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderOperation;
import no.nav.aura.basta.domain.OrderType;
import no.nav.aura.basta.domain.input.bigip.BigIPOrderInput;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.envconfig.client.FasitRestClient;
import no.nav.aura.envconfig.client.ResourceTypeDO;
import no.nav.aura.envconfig.client.rest.ResourceElement;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring-security-unit-test.xml")
public class BigIPOrderRestServiceTest {

    @Inject
    protected AuthenticationManager authenticationManager;

    private BigIPOrderRestService service;

    private RestClient restClient;
    private BigIPClientSetup bigipClientSetup;
    private FasitUpdateService fasitUpdateService;
    private OrderRepository orderRepository;
    private BigIPClient bigipClient;

    @Before
    public void setup() {
        restClient = mock(RestClient.class);
        when(restClient.get(anyString(), eq(Map.class))).thenReturn(Optional.of(new HashMap<>()));
        when(restClient.get(anyString(), eq(List.class))).thenReturn(Optional.of(Lists.newArrayList(new HashMap<>())));

        bigipClientSetup = mock(BigIPClientSetup.class);
        bigipClient = mock(BigIPClient.class);
        when(bigipClient.getVirtualServer(anyString())).thenReturn(Optional.of(new HashMap<>()));
        when(bigipClient.getRules(anyString())).thenReturn(ImmutableMap.of("items", new ArrayList<>()));
        when(bigipClient.deleteRuleFromPolicy(anyString(), eq("dummy_rule"))).thenReturn(new ServerResponse(null, 404, null));
        when(bigipClientSetup.setupBigIPClient(any())).thenReturn(bigipClient);

        fasitUpdateService = mock(FasitUpdateService.class);
        when(fasitUpdateService.createOrUpdateResource(anyLong(), any(ResourceElement.class), any(Order.class))).thenReturn(Optional.of(new ResourceElement(ResourceTypeDO.BaseUrl, "alias")));

        orderRepository = mock(OrderRepository.class);
        when(orderRepository.save(any(Order.class))).thenReturn(new Order(OrderType.BIGIP, OrderOperation.CREATE, new BigIPOrderInput(createBasicRequest())));

        service = new BigIPOrderRestService(orderRepository, fasitUpdateService, mock(FasitRestClient.class), restClient, bigipClientSetup);

        System.setProperty("fasit:resources_v2.url", "https://thefasitresourceapi.com");
        System.setProperty("fasit:environments_v2.url", "https://thefasitenvironmentsapi.com");
        System.setProperty("fasit:applications_v2.url", "https://thefasitapplicationsapi.com");
        System.setProperty("fasit.rest.api.url", "https://theoldfasitapi.com");

        login("user", "user");
    }

    private void login(String username, String password) {
        Authentication token = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Test(expected = BadRequestException.class)
    public void throwsBadRequestWhenMissingContextRoots() {
        Map<String, String> request = createBasicRequest();
        request.put(BigIPOrderInput.USE_HOSTNAME_MATCHING, "false");

        service.createBigIpConfig(request);
    }

    @Test
    public void creatingBigIPConfigWithContextRootsWorks() {
        Map<String, String> request = createBasicRequest();
        request.put(BigIPOrderInput.CONTEXT_ROOTS, "a,b,c");
        request.put(BigIPOrderInput.USE_HOSTNAME_MATCHING, "false");

        service.createBigIpConfig(request);
        verify(bigipClient, times(1)).createRuleOnPolicy(endsWith("_eq_auto"), anyString(), anyString(), anyMap());
        verify(bigipClient, times(1)).createRuleOnPolicy(endsWith("_sw_auto"), anyString(), anyString(), anyMap());
    }

    @Test(expected = BadRequestException.class)
    public void throwsBadRequestWhenMissingHostname() {
        Map<String, String> request = createBasicRequest();
        request.put(BigIPOrderInput.USE_HOSTNAME_MATCHING, "true");

        service.createBigIpConfig(request);
    }

    @Test
    public void creatingBigIPConfigWithHostnameWorks() {
        Map<String, String> request = createBasicRequest();
        request.put(BigIPOrderInput.HOSTNAME, "asdf.adeo.no");
        request.put(BigIPOrderInput.USE_HOSTNAME_MATCHING, "true");

        service.createBigIpConfig(request);

        verify(bigipClient, times(1)).createRuleOnPolicy(endsWith("_hostname_auto"), anyString(), anyString(), anyMap());
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
    public void createsAndRemovesPlaceholderRuleInPolicyWhenNecessary() {
        Map<String, String> request = createBasicRequest();
        request.put(BigIPOrderInput.HOSTNAME, "asdf.adeo.no");
        request.put(BigIPOrderInput.USE_HOSTNAME_MATCHING, "true");

        String policyName = BigIPNamer.createPolicyName("myenv", "u");
        when(bigipClient.getRules(eq(policyName))).thenReturn(ImmutableMap.of("items", Lists.newArrayList()));

        service.createBigIpConfig(request);

        verify(bigipClient, times(1)).createDummyRuleOnPolicy(policyName, "dummy_rule");
        verify(bigipClient, times(1)).deleteRuleFromPolicy(policyName, "dummy_rule");
    }

    @Test
    public void avoidsCreatingPlaceholderRuleInPolicyUnnecessary() {
        Map<String, String> request = createBasicRequest();
        request.put(BigIPOrderInput.HOSTNAME, "asdf.adeo.no");
        request.put(BigIPOrderInput.USE_HOSTNAME_MATCHING, "true");

        String policyName = BigIPNamer.createPolicyName("myenv", "u");
        when(bigipClient.getRules(eq(policyName))).thenReturn(ImmutableMap.of("items", Lists.newArrayList(ImmutableMap.of("name", "someOtherRule"))));

        service.createBigIpConfig(request);

        verify(bigipClient, times(0)).createDummyRuleOnPolicy(anyString(), anyString());
        verify(bigipClient, times(1)).deleteRuleFromPolicy(policyName, "dummy_rule");
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