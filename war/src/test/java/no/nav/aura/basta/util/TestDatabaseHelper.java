package no.nav.aura.basta.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import javax.sql.DataSource;

import no.nav.aura.basta.spring.SpringOracleUnitTestConfig;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.flyway.core.Flyway;

public abstract class TestDatabaseHelper {

    private static Logger log = LoggerFactory.getLogger(TestDatabaseHelper.class);

    public static final String DB_MIGRATION_BASTA_DB = "/db/migration/bastaDB";

    /** Uninstantiateable */
    private TestDatabaseHelper() {
    }

    public static void annihilateAndRebuildDatabaseSchema(DataSource dataSource) {
        TestDatabaseHelper.updateDatabaseSchema(dataSource, true, DB_MIGRATION_BASTA_DB);
    }

    public static void updateDatabaseSchema(DataSource dataSource, String... locations) {
        updateDatabaseSchema(dataSource, false, locations);
    }

    public static void updateDatabaseSchema(DataSource dataSource) {
        updateDatabaseSchema(dataSource, false, DB_MIGRATION_BASTA_DB);
    }

    private static void updateDatabaseSchema(DataSource dataSource, boolean annihilate, String... locations) {
        Flyway flyway = new Flyway();

        flyway.setDataSource(dataSource);
        flyway.setLocations(locations);
        if (annihilate) {
            flyway.clean();
        }

        // Skip migrations if in-memory/H2, our scripts are not compatible
        if (!Boolean.valueOf(System.getProperty("useH2"))) {
            int migrationsApplied = flyway.migrate();
            log.info(migrationsApplied + " flyway migration scripts ran");
        }
    }

    public static void createTemporaryDatabase() {
        String schemaName = TestDatabaseHelper.createTemporarySchema();
        System.getProperties().setProperty(SpringOracleUnitTestConfig.TEMPORARY_DATABASE_SCHEMA, schemaName);
    }

    public static DataSource getAdminDataSource() {
        return TestDatabaseHelper.createDataSource("oracle", SpringOracleUnitTestConfig.URL, "admin", "Start1");
    }

    public static String createTemporarySchema() {
        try (Connection connection = getAdminDataSource().getConnection()) {
            Statement statement = connection.createStatement();
            String schemaName = ("JUNIT" + UUID.randomUUID().toString().replaceAll("-", "")).substring(0, 16);
            statement.execute("CREATE USER " + schemaName + " PROFILE DEFAULT IDENTIFIED BY password DEFAULT TABLESPACE basta QUOTA 50M ON basta TEMPORARY TABLESPACE TEMP ACCOUNT UNLOCK");
            statement.execute("GRANT CONNECT TO " + schemaName);
            statement.execute("GRANT resource TO " + schemaName);
            statement.execute("GRANT CREATE TABLE TO " + schemaName);
            return schemaName;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void dropTemporaryDatabase() {
        String schemaName = System.getProperty(SpringOracleUnitTestConfig.TEMPORARY_DATABASE_SCHEMA);
        if (schemaName != null) {
            try (Connection connection = getAdminDataSource().getConnection()) {
                Statement statement = connection.createStatement();
                statement.execute("drop user " + schemaName + " cascade");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static DataSource createDataSource(String type, String url, String username, String password) {
        if ("h2".equalsIgnoreCase(type)) {
            System.setProperty("useH2", "true");
        }
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMaxWait(20000);
        return ds;
    }

}
