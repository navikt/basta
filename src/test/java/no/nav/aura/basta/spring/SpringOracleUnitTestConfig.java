package no.nav.aura.basta.spring;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@Import({ SpringUnitTestConfig.class })
public class SpringOracleUnitTestConfig {

//	public static final String TEMPORARY_DATABASE_SCHEMA = "TEMPORARY_DATABASE_SCHEMA";
//    public static final String URL = "jdbc:oracle:thin:@dmv01-scan.adeo.no:1521/basta_u1_ha";
    public static String URL;
	@BeforeAll
	public static void setup() {
		System.setProperty("spring.flyway.enabled", "true");
//		System.setProperty("flyway.cleanDisabled", "false");
	}

//	@Bean(name = "dataSource")
//	public DataSource dataSource() {
//		System.getProperties().remove("BASTADB_TYPE");
//		String schemaName = System.getProperty(TEMPORARY_DATABASE_SCHEMA);
//		if (schemaName == null) {
//			throw new RuntimeException("Missing temporary database schema");
//		}
////		DataSource dataSource = TestDatabaseHelper.createDataSource("h2", URL, schemaName, "password");
//		DataSource dataSource = TestDatabaseHelper.createDataSource("oracle", TEMPORARY_DATABASE_SCHEMA, schemaName, "password");
//		TestDatabaseHelper.annihilateAndRebuildDatabaseSchema(dataSource);
//		return dataSource;
//	}
}
