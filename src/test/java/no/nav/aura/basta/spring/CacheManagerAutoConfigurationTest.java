package no.nav.aura.basta.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

public class CacheManagerAutoConfigurationTest {

    @Configuration
    @EnableCaching
    static class CachingConfig {
    }

    @Test
    public void cacheManagerIsAutoConfiguredAndProvidesOrdersCache() throws Exception {
        // Test resources shadow the classpath application.properties, so read the production file directly
        Properties production = new Properties();
        try (InputStream in = Files.newInputStream(Path.of("src/main/resources/application.properties"))) {
            production.load(in);
        }

        new ApplicationContextRunner()
                .withPropertyValues("spring.cache.type=" + production.getProperty("spring.cache.type"),
                        "spring.cache.cache-names=" + production.getProperty("spring.cache.cache-names"))
                .withConfiguration(AutoConfigurations.of(CacheAutoConfiguration.class))
                .withUserConfiguration(CachingConfig.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(CacheManager.class);
                    assertThat(context.getBean(CacheManager.class).getCache("orders")).isNotNull();
                    Cache orders = context.getBean(CacheManager.class).getCache("orders");
                    List<Object> value = new ArrayList<>(List.of(new Object()));
                    orders.put("key", value);
                    assertThat(orders.get("key").get()).isNotNull();
                });
    }
}
