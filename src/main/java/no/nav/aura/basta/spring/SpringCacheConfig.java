package no.nav.aura.basta.spring;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import no.nav.aura.basta.repository.OrderRepository;

@Configuration
public class SpringCacheConfig {

    // Evictions are deferred until the surrounding transaction commits, so a concurrent reader
    // cannot re-populate the cache with stale data before the write is visible.
    @Bean
    public CacheManager cacheManager() {
        return new TransactionAwareCacheManagerProxy(new ConcurrentMapCacheManager(OrderRepository.ORDERS_CACHE));
    }
}
