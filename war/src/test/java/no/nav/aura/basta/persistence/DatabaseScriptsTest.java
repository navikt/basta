package no.nav.aura.basta.persistence;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.domain.SystemNotification;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.order.VmOrderTestData;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.repository.SystemNotificationRepository;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDetailsDO;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SpringOracleUnitTestConfig.class })
@Transactional
@TransactionConfiguration(defaultRollback = false)
public class DatabaseScriptsTest {

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private DataSource dataSource;

    private static BasicDataSource dataSourceToClose;

    @Inject
    private SystemNotificationRepository systemNotificationRepository;

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
        Order order = createOrderWithExternalId();
        orderRepository.save(order);
        MapOperations input = new MapOperations(new HashMap<String, String>());
        input.put(VMOrderInput.APPLICATION_MAPPING_NAME, "myApp");
        input.put(VMOrderInput.SERVER_COUNT, "1");
        input.put(VMOrderInput.BPM_CELL_DATASOURCE_ALIAS, "døll");

        order.setInput(input);
        orderRepository.save(order);

        OrderDetailsDO orderDetails = new OrderDetailsDO();
        orderDetails.setApplicationMappingName("myApp");
        orderDetails.setServerCount(1);
        orderDetails.setCellDatasource("døll");

        orderRepository.save(order);
        assertThat(Sets.newHashSet(orderRepository.findAll()).size(), equalTo(1));

    }

    @Test
    public void sanityTest() {
        Order order = createOrderWithExternalId();
        orderRepository.save(order);
        MapOperations input = new MapOperations(new HashMap<String, String>());
        input.put("testkey", "testValue");
        order.setInput(input);
        orderRepository.save(order);
        assertThat(Sets.newHashSet(orderRepository.findAll()).size(), equalTo(1));
        assertThat(order.getInputAs(VMOrderInput.class).get("testkey"), is(equalTo("testValue")));

    }

    @Test
    public void orderStatusTest() {
        Order order = orderRepository.save(createOrderWithExternalId());
        order.addStatusLog(new OrderStatusLog("Basta", "a", "b"));
        order.addStatusLog(new OrderStatusLog("Orchestrator", "d", "e"));
        orderRepository.save(order);

        Order one = orderRepository.findOne(order.getId());
        assertThat(one.getStatusLogs(), hasSize(2));
    }

    @Test
    public void sholdBeAbleToGetNotifications() throws Exception {
        systemNotificationRepository.save(SystemNotification.newSystemNotification("message"));
        systemNotificationRepository.save(SystemNotification.newSystemNotification("message"));
        SystemNotification message = systemNotificationRepository.save(SystemNotification.newSystemNotification("message"));
        message.setInactive();

        assertThat(Lists.newArrayList(systemNotificationRepository.findAll()), hasSize(3));
        assertThat(systemNotificationRepository.findByActiveTrue(), hasSize(2));
    }

    @Test
    public void findnextAndPreviousOrder() {
        Order first = orderRepository.save(createOrderWithExternalId());
        Order a = orderRepository.save(createOrderWithExternalId());
        Order b = orderRepository.save(createOrderWithExternalId());
        Order c = orderRepository.save(createOrderWithExternalId());
        Order last = orderRepository.save(createOrderWithExternalId());
        assertThat(orderRepository.findPreviousId(first.getId()), is(nullValue()));
        assertThat(orderRepository.findPreviousId(b.getId()), is(equalTo(a.getId())));
        assertThat(orderRepository.findNextId(b.getId()), is(equalTo(c.getId())));
        assertThat(orderRepository.findNextId(last.getId()), is(nullValue()));
    }

    private Order createOrderWithExternalId() {
        Order order = VmOrderTestData.newProvisionOrderWithDefaults(NodeType.JBOSS);
        order.setExternalId("1");
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
        Order order = orderRepository.save(createOrderWithExternalId());
        List<Long> list = new ArrayList<>();
        for (int i = 0; i < numberOfLogStatuses; i++) {
            OrderStatusLog log = new OrderStatusLog("x", "a", "b");
            order.addStatusLog(log);
            orderRepository.save(order);
            list.add(log.getId());
        }
        return new Tuple<>(order.getId(), list);
    }

}
