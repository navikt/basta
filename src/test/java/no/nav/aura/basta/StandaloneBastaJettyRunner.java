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
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@SpringBootApplication
@ComponentScan(basePackageClasses = RootPackage.class, excludeFilters = {@Filter
        (Configuration.class), @Filter(SpringBootApplication.class)})
@Import({StandaloneRunnerTestConfig.class})
public class StandaloneBastaJettyRunner implements EmbeddedServletContainerCustomizer{

    private final ApplicationContext context;

    @Inject
    public StandaloneBastaJettyRunner(ApplicationContext context) {
        assertNotNull(context, "Context can not be null");
        this.context = context;
        createTestData();
    }

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        container.setPort(1337);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("flyway.enabled", "false");
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
