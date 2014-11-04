package no.nav.aura.basta.persistence;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;
import java.util.*;

import javax.inject.Inject;
import javax.sql.DataSource;

import com.google.common.collect.Lists;
import no.nav.aura.basta.rest.OrderDetailsDO;
import no.nav.aura.basta.spring.SpringOracleUnitTestConfig;
import no.nav.aura.basta.util.TestDatabaseHelper;

import no.nav.aura.basta.util.Tuple;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringOracleUnitTestConfig.class })
@Transactional
@TransactionConfiguration(defaultRollback = false)
public class DatabaseScriptsTest {

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private NodeRepository nodeRepository;


    @Inject
    private DataSource dataSource;

    private static BasicDataSource dataSourceToClose;

    @Before
    public void createData() {
        dataSourceToClose = (BasicDataSource) dataSource;
        TestDatabaseHelper.annihilateAndRebuildDatabaseSchema(dataSource);
    }

    @BeforeClass
    public static void createDatabase() {
        TestDatabaseHelper.createTemporaryDatabase();
    }

    @AfterClass
    public static void deleteDatabase() throws SQLException {
        if (dataSourceToClose != null) {
            dataSourceToClose.close();
        }
        TestDatabaseHelper.dropTemporaryDatabase();
    }

    @Test
    public void test() {
        Order order = createOrderWithOrchestratorOrderId();
        orderRepository.save(order);
        OrderDetailsDO orderDetails = new OrderDetailsDO();
        orderDetails.setApplicationMappingName("myApp");
        orderDetails.setServerCount(1);
        orderDetails.setCellDatasource("døll");
        order.setSettings(new Settings(orderDetails));
        orderRepository.save(order);
        assertThat(Sets.newHashSet(orderRepository.findAll()).size(), equalTo(1));
        assertThat(order.getSettings(), is(notNullValue()));

    }

    @Test
    public void orderStatusTest() {
        Order order = orderRepository.save(createOrderWithOrchestratorOrderId());
        order.addStatusLog(new OrderStatusLog("Basta", "a", "b", "c"));
        order.addStatusLog(new OrderStatusLog("Orchestrator", "d", "e", "f"));
        orderRepository.save(order);

        Order one = orderRepository.findOne(order.getId());
        assertThat(one.getStatusLogs(), hasSize(2));
    }

    @Test
    public void findnextAndPreviousOrder() {
        Order first = orderRepository.save(createOrderWithOrchestratorOrderId());
        Order a = orderRepository.save(createOrderWithOrchestratorOrderId());
        Order b = orderRepository.save(createOrderWithOrchestratorOrderId());
        Order c = orderRepository.save(createOrderWithOrchestratorOrderId());
        Order last = orderRepository.save(createOrderWithOrchestratorOrderId());
        assertThat(orderRepository.findPreviousId(first.getId()),is(nullValue()));
        assertThat(orderRepository.findPreviousId(b.getId()), is(equalTo(a.getId())));
        assertThat(orderRepository.findNextId(b.getId()), is(equalTo(c.getId())));
        assertThat(orderRepository.findNextId(last.getId()), is(nullValue()));
    }

    private Order createOrderWithOrchestratorOrderId() {
        Order order = Order.newProvisionOrder(NodeType.APPLICATION_SERVER);
        order.setOrchestratorOrderId("1");
        return order;
    }

    @Test
    public void shouldUseDifferentSequences() throws Exception {
        LinkedList<Long> orderIds = new LinkedList<>();
        LinkedList<Long> logIds = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            Tuple<Long, List<Long>> ids = createOrderWithLogStatus(20);
            orderIds.add(ids.fst);
            logIds.addAll(ids.snd);
        }
        assertListIsNiceAndSortedWithoutGaps(orderIds);
        assertListIsNiceAndSortedWithoutGaps(logIds);
    }

    private void assertListIsNiceAndSortedWithoutGaps(LinkedList<Long> list) {
        Long last = list.peekLast();
        while (list.peek() != null) {
            Long current = list.pop();
            if (list.peek() != null) {
                assertThat(current, is(equalTo(list.peek() - 1)));
            } else {
                assertThat(current, is(equalTo(last)));
            }
        }
    }


    private Tuple<Long, List<Long>> createOrderWithLogStatus(int numberOfLogStatuses) {
        Order order = orderRepository.save(createOrderWithOrchestratorOrderId());
        List<Long> list = new ArrayList<>();
        for (int i = 0; i < numberOfLogStatuses; i++) {
            OrderStatusLog log = new OrderStatusLog("x", "a", "b", "c");
            order.addStatusLog(log);
            orderRepository.save(order);
            list.add(log.getId());
        }
        return new Tuple<>(order.getId(), list);
    }


}
