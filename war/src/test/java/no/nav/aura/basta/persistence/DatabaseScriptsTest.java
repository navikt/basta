package no.nav.aura.basta.persistence;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
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
    private SettingsRepository settingsRepository;

    @Inject
    private NodeRepository nodeRepository;

    @Inject
    private OrderStatusLogRepository orderStatusLogRepository;

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
        Order order = new Order(NodeType.APPLICATION_SERVER);
        orderRepository.save(order);
        OrderDetailsDO orderDetails = new OrderDetailsDO();
        orderDetails.setServerCount(1);
        orderDetails.setCellDatasource("døll");
        settingsRepository.save(new Settings(order, orderDetails));
        assertThat(Sets.newHashSet(orderRepository.findAll()).size(), equalTo(1));
        Set<Settings> all = Sets.newHashSet(settingsRepository.findAll());
        assertThat(all.size(), equalTo(1));
        nodeRepository.save(new Node());

    }

    @Test
    public void orderStatusTest() {
        Order order = orderRepository.save(new Order(NodeType.APPLICATION_SERVER));
        orderStatusLogRepository.save(new OrderStatusLog(order, "a", "b", "c"));
        orderStatusLogRepository.save(new OrderStatusLog(order,"d", "e", "f"));
        assertThat(Lists.newArrayList(orderStatusLogRepository.findByOrderId(order.getId())), hasSize(2));
    }

    @Test
    public void findnextAndPreviousOrder() {
        Order first = orderRepository.save(new Order(NodeType.APPLICATION_SERVER));
        Order a = orderRepository.save(new Order(NodeType.APPLICATION_SERVER));
        Order b = orderRepository.save(new Order(NodeType.APPLICATION_SERVER));
        Order c = orderRepository.save(new Order(NodeType.APPLICATION_SERVER));
        Order last = orderRepository.save(new Order(NodeType.APPLICATION_SERVER));
        assertThat(orderRepository.findPreviousId(first.getId()),is(nullValue()));
        assertThat(orderRepository.findPreviousId(b.getId()), is(equalTo(a.getId())));
        assertThat(orderRepository.findNextId(b.getId()), is(equalTo(c.getId())));
        assertThat(orderRepository.findNextId(last.getId()), is(nullValue()));
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
        Order order = orderRepository.save(new Order(NodeType.APPLICATION_SERVER));
        List<Long> list = new ArrayList<>();
        for (int i = 0; i < numberOfLogStatuses; i++) {
            OrderStatusLog log = orderStatusLogRepository.save(new OrderStatusLog(order, "a", "b", "c"));
            list.add(log.getId());
        }
        return new Tuple<>(order.getId(), list);
    }


}
