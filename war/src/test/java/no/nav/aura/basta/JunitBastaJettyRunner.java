package no.nav.aura.basta;

import java.io.File;

import javax.sql.DataSource;

import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.OrderStatus;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.domain.result.vm.ResultStatus;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.repository.OrderRepository;

public class JunitBastaJettyRunner extends BastaJettyRunner {

	public JunitBastaJettyRunner() {
		super(0, new File(getProjectRoot(), "src/test/resources/junit-override-web.xml").getPath());
	}

	@Override
	protected DataSource createDatasource() {
		return createDataSource("h2", "jdbc:h2:mem:bastajunit", "sa", "");
	}

	public void createTestData() {

		OrderRepository orderRepository = getSpringContext().getBean(OrderRepository.class);

		NodeType applicationServer = NodeType.JBOSS;
		Order order = orderRepository.save(Order.newProvisionOrderUsedOnlyForTestingPurposesRefactorLaterIPromise_yeahright(applicationServer));

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
