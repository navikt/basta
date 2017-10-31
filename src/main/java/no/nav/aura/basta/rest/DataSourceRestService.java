package no.nav.aura.basta.rest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.spring.SpringConfig;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


@Component
@Path("/datasource")
public class DataSourceRestService {

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private OrderRepository orderRepository;

    private static final Logger logger = LoggerFactory.getLogger(SpringConfig.class);


    @GET
    @NoCache
    public Map<String, String> getDataSourceConnection() {
        DataSource ds = applicationContext.getBean(DataSource.class);
        HashMap<String, String> dataSourceConnection = new HashMap<>();
        try (Connection connection = ds.getConnection()) {
            dataSourceConnection.put("datasource",connection.getMetaData().getUserName() + "@ " + connection.getMetaData().getURL());
        } catch (SQLException e) {
            logger.warn("Error retrieving database user metadata", e);
        }
        return dataSourceConnection;
    }

    @GET
    @NoCache
    @Path("/alive")
    public Map<String, Boolean> isAlive() {
        HashMap<String, Boolean> alive = new HashMap<>();
        alive.put("dbAlive", checkAliveTimeoutAfter(3));
        return alive;
    }


    public Boolean checkAliveTimeoutAfter(final Integer timeout) {
        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Boolean> task = new Callable<Boolean>() {
            public Boolean call() {
                orderRepository.count();
                return true;
            }
        };

        Future<Boolean> future = executor.submit(task);
        try {
            return future.get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException | RuntimeException e) {
            return false;
        } finally {
            future.cancel(false);
        }
    }
}
