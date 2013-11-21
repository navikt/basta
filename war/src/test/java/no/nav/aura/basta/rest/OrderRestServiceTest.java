package no.nav.aura.basta.rest;

import javax.inject.Inject;

import no.nav.aura.basta.order.OrderV1FactoryTest;
import no.nav.aura.basta.rest.SettingsDO.EnvironmentClassDO;
import no.nav.aura.basta.spring.SpringUnitTestConfig;
import no.nav.aura.basta.util.Effect;
import no.nav.aura.basta.util.SpringRunAs;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringUnitTestConfig.class })
public class OrderRestServiceTest {

    @Inject
    private OrdersRestService ordersRestService;

    @Inject
    private AuthenticationManager authenticationManager;

    @Test(expected = UnauthorizedException.class)
    public void notAuthorised() throws Exception {
        orderWithEnvironmentClass(EnvironmentClassDO.prod);
    }

    @Test
    public void authorised() throws Exception {
        orderWithEnvironmentClass(EnvironmentClassDO.utv);
    }

    @SuppressWarnings("serial")
    private void orderWithEnvironmentClass(final EnvironmentClassDO environmentClass) {
        SpringRunAs.runAs(authenticationManager, "admin", "admin", new Effect() {
            public void perform() {
                SettingsDO settings = OrderV1FactoryTest.createRequest1Settings();
                settings.setEnvironmentClass(environmentClass);
                ordersRestService.postOrder(settings);
            }
        });
    }

}
