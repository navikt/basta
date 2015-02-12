package no.nav.aura.basta;

import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.ResultStatus;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.repository.OrderRepository;

import javax.sql.DataSource;
import java.io.File;

public class StandaloneBastaJettyRunner extends BastaJettyRunner {

    public StandaloneBastaJettyRunner(int port, String overrideDescriptor) {
        super(port, overrideDescriptor);
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        StandaloneBastaJettyRunner jetty = new StandaloneBastaJettyRunner(1337, new File(getProjectRoot(), "src/test/resources/override-web.xml").getPath());
        jetty.start();
        //jetty.createTestData();
        jetty.server.join();
    }

    public void createTestData() {

        OrderRepository orderRepository = getSpringContext().getBean(OrderRepository.class);

        NodeType applicationServer = NodeType.APPLICATION_SERVER;
        Order order = orderRepository.save(Order.newProvisionOrder(applicationServer));

        MapOperations input = MapOperations.single(VMOrderInput.ENVIRONMENT_CLASS, EnvironmentClass.u);

        order.setInput(input);
        VMOrderResult result = order.getResultAs(VMOrderResult.class);
        result.addHostnameWithStatusAndNodeType("foo.devillo.no", ResultStatus.ACTIVE);
        result.addHostnameWithStatusAndNodeType("bar.devillo.no", ResultStatus.ACTIVE);
        orderRepository.save(order);
    }

    @Override
    protected DataSource createDatasource() {
        return createDataSource("h2", "jdbc:h2:mem:basta", "sa", "");
    }

}
