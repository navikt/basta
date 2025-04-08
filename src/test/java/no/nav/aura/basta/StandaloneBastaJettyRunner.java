package no.nav.aura.basta;

import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.domain.result.vm.ResultStatus;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.order.VmOrderTestData;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.spring.StandaloneRunnerTestConfig;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import jakarta.inject.Inject;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@SpringBootApplication
@ComponentScan(excludeFilters = {@ComponentScan.Filter(Configuration.class), @ComponentScan.Filter(SpringBootApplication.class)})
@Import(StandaloneRunnerTestConfig.class)
public class StandaloneBastaJettyRunner implements WebServerFactoryCustomizer<JettyServletWebServerFactory> {

    private final ApplicationContext context;

//    @Inject
//    OrderRepository orderRepository;

    @Inject
    public StandaloneBastaJettyRunner(ApplicationContext context) {
        assertNotNull(context, "Context can not be null");
        this.context = context;
        createTestData();
    }

    @Override
    public void customize(JettyServletWebServerFactory container) {
        container.setPort(1337);
    }

    public static void main(String[] args) {
        // Default value has changed in Spring5, need to allow overriding of beans in tests
//        System.setProperty("spring.main.allow-bean-definition-overriding", "true");

        SpringApplication springApp = new SpringApplication(StandaloneBastaJettyRunner.class);
        springApp.setBannerMode(Banner.Mode.OFF);
        springApp.run(args);
    }

    public void createTestData() {
        OrderRepository orderRepository = context.getBean(OrderRepository.class);
        NodeType applicationServer = NodeType.JBOSS;
        Order order = orderRepository.save(VmOrderTestData.newProvisionOrderWithDefaults(applicationServer));

        MapOperations input = MapOperations.single(VMOrderInput.ENVIRONMENT_CLASS, EnvironmentClass.u);

        order.setInput(input);
        VMOrderResult result = order.getResultAs(VMOrderResult.class);
        result.addHostnameWithStatusAndNodeType("foo.devillo.no", ResultStatus.ACTIVE, NodeType.JBOSS);
        order.setExternalId("someid");
        order.setStatus(OrderStatus.SUCCESS);
        orderRepository.save(order);
    }
}
