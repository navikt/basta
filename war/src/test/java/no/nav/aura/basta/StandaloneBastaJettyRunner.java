package no.nav.aura.basta;

import java.io.File;

import javax.sql.DataSource;

import no.nav.aura.basta.persistence.*;
import no.nav.aura.basta.vmware.orchestrator.request.Vm;

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
        jetty.createTestData();
        jetty.server.join();
    }

    public void createTestData() {
        NodeRepository nodeRepository = getSpringContext().getBean(NodeRepository.class);
        OrderRepository orderRepository = getSpringContext().getBean(OrderRepository.class);

        Order order = orderRepository.save(new Order(NodeType.APPLICATION_SERVER));
        Settings settings = new Settings();
        settings.setEnvironmentClass(EnvironmentClass.u);
        order.setSettings(settings);

        Node node1 = new Node(order, "foo.devillo.no", null, 1, 1024, "datasenter", Vm.MiddleWareType.ap, "asdf");
        Node node2 = new Node(order, "bar.devillo.no", null, 1, 1024, "datasenter", Vm.MiddleWareType.ap, "asdf2");
        node1.setOrder(order);
        node2.setOrder(order);

        nodeRepository.save(node1);
        nodeRepository.save(node2);
        orderRepository.save(order);
    }

    @Override
    protected DataSource createDatasource() {
        return createDataSource("h2", "jdbc:h2:mem:basta", "sa", "");
    }

}
