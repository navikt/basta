package no.nav.aura.basta.persistence;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

import no.nav.aura.basta.rest.OrderDetailsDO;
import no.nav.aura.basta.spring.SpringOracleUnitTestConfig;
import no.nav.aura.basta.util.TestDatabaseHelper;

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
        Order order = new Order();
        orderRepository.save(order);
        OrderDetailsDO orderDetails = new OrderDetailsDO();
        orderDetails.setServerCount(1);
        orderDetails.setCellDatasource("døll");
        settingsRepository.save(new Settings(order, orderDetails));
        assertThat(Sets.newHashSet(orderRepository.findAll()).size(), equalTo(1));
        assertThat(Sets.newHashSet(settingsRepository.findAll()).size(), equalTo(1));
    }
}
