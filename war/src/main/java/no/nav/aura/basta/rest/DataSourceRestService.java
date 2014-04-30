package no.nav.aura.basta.rest;

import com.google.common.collect.Maps;
import no.nav.aura.basta.persistence.NodeRepository;
import no.nav.aura.basta.spring.SpringConfig;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Component
@Path("/datasource")
public class DataSourceRestService {

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private NodeRepository nodeRepository;

    private static final Logger logger = LoggerFactory.getLogger(SpringConfig.class);


    @GET
    @NoCache
    public String getDataSourceConnection() {
        DataSource ds = applicationContext.getBean(DataSource.class);
        String dataSourceConnection = "";
        try (Connection connection = ds.getConnection()) {
            dataSourceConnection = connection.getMetaData().getUserName() + "@ " + connection.getMetaData().getURL();
        } catch (SQLException e) {
            logger.warn("Error retrieving database user metadata", e);
        }
        return dataSourceConnection;
    }

    @GET
    @NoCache
    @Path("/alive")
    public Map<String, Boolean> isAlive() {
        HashMap<String, Boolean> alive = Maps.newHashMap();
        alive.put("dbAlive", checkAliveTimeoutAfter(3));
        return alive;
    }


    public Boolean checkAliveTimeoutAfter(final Integer timeout) {
        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Boolean> task = new Callable<Boolean>() {
            public Boolean call() {
                nodeRepository.count();
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
