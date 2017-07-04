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

import javax.sql.DataSource;
import java.io.File;

public class JunitBastaJettyRunner extends BastaJettyRunner {

	public JunitBastaJettyRunner() {
		super(0, new File(getProjectRoot(), "src/test/resources/junit-override-web.xml").getPath());
	}

	@Override
	protected DataSource createDatasource() {
		return createDataSource("h2", "jdbc:h2:mem:bastajunit", "sa", "");
	}

	@Override
	public void setOrchestratorConfigProperties() {
		System.setProperty("rest.orchestrator.provision.url", "http://provisionurl.com");
		System.setProperty("rest.orchestrator.decomission.url", "http://provisionurl.com");
		System.setProperty("rest.orchestrator.startstop.url", "http://provisionurl.com");
		System.setProperty("rest.orchestrator.modify.url", "http://provisionurl.com");

		System.setProperty("user.orchestrator.username", "orchestratorUser");
		System.setProperty("user.orchestrator.password", "orchestratorPassword");
	}

	public void createTestData() {

		OrderRepository orderRepository = getSpringContext().getBean(OrderRepository.class);

		NodeType applicationServer = NodeType.JBOSS;
        Order order = orderRepository.save(VmOrderTestData.newProvisionOrderWithDefaults(applicationServer));

		MapOperations input = MapOperations.single(VMOrderInput.ENVIRONMENT_CLASS, EnvironmentClass.u);

		order.setInput(input);
		VMOrderResult result = order.getResultAs(VMOrderResult.class);
		result.addHostnameWithStatusAndNodeType("foo.devillo.no", ResultStatus.ACTIVE, NodeType.JBOSS);
		order.setStatus(OrderStatus.SUCCESS);
		order.setExternalId("someid");
		Order save = orderRepository.save(order);
		System.out.println(save.getId() + " " + save);
	}

}
