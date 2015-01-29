package no.nav.aura.basta;

import java.io.File;

import javax.sql.DataSource;

import no.nav.aura.basta.domain.input.Input;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.input.vm.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.NodeStatus;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.domain.result.vm.VMOrderResult;
import no.nav.aura.basta.persistence.*;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.backend.vmware.orchestrator.request.Vm;

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

        Input input = Input.single(VMOrderInput.ENVIRONMENT_CLASS, EnvironmentClass.u);

        order.setInput(input);
        VMOrderResult result = order.getResultAs(VMOrderResult.class);
        result.addHostnameWithStatus("foo.devillo.no", NodeStatus.ACTIVE);
        result.addHostnameWithStatus("bar.devillo.no", NodeStatus.ACTIVE);


        //Node node1 = new Node(order,applicationServer, "foo.devillo.no", null, 1, 1024, "datasenter", Vm.MiddleWareType.ap, "asdf");
        //Node node2 = new Node(order,applicationServer, "bar.devillo.no", null, 1, 1024, "datasenter", Vm.MiddleWareType.ap, "asdf2");
        //node1.addOrder(order);
        //node2.addOrder(order);

        //nodeRepository.save(node1);
        //nodeRepository.save(node2);
        orderRepository.save(order);
    }

    @Override
    protected DataSource createDatasource() {
        return createDataSource("h2", "jdbc:h2:mem:basta", "sa", "");
    }

}
