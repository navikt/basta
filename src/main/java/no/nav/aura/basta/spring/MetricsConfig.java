package no.nav.aura.basta.spring;

import io.prometheus.client.CollectorRegistry;

import io.prometheus.client.hotspot.DefaultExports;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    CollectorRegistry metricRegistry() {
        DefaultExports.initialize();
        return CollectorRegistry.defaultRegistry;
    }

}
