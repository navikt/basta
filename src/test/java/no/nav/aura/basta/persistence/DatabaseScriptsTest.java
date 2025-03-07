package no.nav.aura.basta.persistence;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.ModelEntity;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.SystemNotification;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import no.nav.aura.basta.order.VmOrderTestData;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.repository.SystemNotificationRepository;
import no.nav.aura.basta.spring.SpringOracleUnitTestConfig;
import no.nav.aura.basta.util.TestDatabaseHelper;
import no.nav.aura.basta.util.Tuple;
import org.apache.commons.dbcp.BasicDataSource;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import javax.ws.rs.NotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { SpringOracleUnitTestConfig.class })
@Transactional
@Rollback
public class DatabaseScriptsTest {

    @Inject
    private OrderRepository orderRepository;

    @Named("dataSource")
    @Inject
    private DataSource dataSource;

    private static BasicDataSource dataSourceToClose;

    @Inject
    private SystemNotificationRepository systemNotificationRepository;

    @BeforeEach
    public void createData() {
        dataSourceToClose = (BasicDataSource) dataSource;
        TestDatabaseHelper.annihilateAndRebuildDatabaseSchema(dataSource);
    }

    @BeforeAll
    public static void createDatabase() {
        TestDatabaseHelper.createTemporaryDatabase();
    }

    @AfterAll
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
        MapOperations input = new MapOperations(new HashMap<>());
        input.put(VMOrderInput.APPLICATION_MAPPING_NAME, "myApp");
        input.put(VMOrderInput.SERVER_COUNT, "1");
        input.put(VMOrderInput.DESCRIPTION, "døll");

        order.setInput(input);
        orderRepository.save(order);

        orderRepository.save(order);
        MatcherAssert.assertThat(Sets.newHashSet(orderRepository.findAll()).size(), equalTo(1));

    }

    @Test
    public void sanityTest() {
        Order order = createOrderWithExternalId();
        orderRepository.save(order);
        MapOperations input = new MapOperations(new HashMap<>());
        input.put("testkey", "testValue");
        order.setInput(input);
        orderRepository.save(order);
        MatcherAssert.assertThat(Sets.newHashSet(orderRepository.findAll()).size(), equalTo(1));
        MatcherAssert.assertThat(order.getInputAs(VMOrderInput.class).get("testkey"), is(equalTo("testValue")));

    }

    @Test
    public void orderStatusTest() {
        Order order = orderRepository.save(createOrderWithExternalId());
        order.addStatuslogInfo("Basta");
        order.addStatuslogInfo("Orchestrator");
        orderRepository.save(order);

        Order one = orderRepository.findById(order.getId()).orElseThrow(() -> new NotFoundException("Entity " +
                "not found " + order.getId()));
        MatcherAssert.assertThat(one.getStatusLogs(), hasSize(2));
    }

    @Test
    public void sholdBeAbleToGetNotifications() {
        systemNotificationRepository.save(SystemNotification.newSystemNotification("message"));
        systemNotificationRepository.save(SystemNotification.newSystemNotification("message"));
        SystemNotification message = systemNotificationRepository.save(SystemNotification.newSystemNotification("message"));
        message.setInactive();

        MatcherAssert.assertThat(Lists.newArrayList(systemNotificationRepository.findAll()), hasSize(3));
        MatcherAssert.assertThat(systemNotificationRepository.findByActiveTrue(), hasSize(2));
    }

    private Order createOrderWithExternalId() {
        Order order = VmOrderTestData.newProvisionOrderWithDefaults(NodeType.JBOSS);
        order.setExternalId("1");
        return order;
    }

    @Test
    public void shouldUseDifferentSequences() {
        LinkedList<Long> orderIds = new LinkedList<>();
        LinkedList<Long> logIds = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            Tuple<Long, List<Long>> ids = createOrderWithLogStatus();
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
                MatcherAssert.assertThat(current, is(equalTo(list.peek() - 1)));
            } else {
                MatcherAssert.assertThat(current, is(equalTo(last)));
            }
        }
    }

    private Tuple<Long, List<Long>> createOrderWithLogStatus() {
        Order order = orderRepository.save(createOrderWithExternalId());
        for (int i = 0; i < 20; i++) {
            order.addStatuslogInfo("x  "+ i);
            orderRepository.save(order);
        }
        return new Tuple<>(order.getId(), order.getStatusLogs().stream().map(ModelEntity::getId).collect(Collectors.toList()));
    }

}
