package no.nav.aura.basta.spring;

import no.nav.aura.basta.util.TestDatabaseHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@Import({ SpringUnitTestConfig.class })
public class SpringOracleUnitTestConfig {

    public static final String URL = "jdbc:oracle:thin:@(DESCRIPTION=(FAILOVER=on)(CONNECT_TIMEOUT= 15)(RETRY_COUNT=20)(RETRY_DELAY=3)(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=d26dbfl022.test.local)(PORT=1521)))(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=d26dbfl024.test.local)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=basta_u1_ha)))";
    public static final String TEMPORARY_DATABASE_SCHEMA = "TEMPORARY_DATABASE_SCHEMA";

    @Bean(name = "dataSource")
    public DataSource dataSource() {
        System.getProperties().remove("BASTADB_TYPE");
        String schemaName = System.getProperty(TEMPORARY_DATABASE_SCHEMA);
        if (schemaName == null) {
            throw new RuntimeException("Missing temporary database schema");
        }
        DataSource dataSource = TestDatabaseHelper.createDataSource("oracle", URL, schemaName, "password");
        TestDatabaseHelper.annihilateAndRebuildDatabaseSchema(dataSource);
        return dataSource;
    }
}
