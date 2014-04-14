package no.nav.aura.basta.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface OrderStatusLogRepository extends CrudRepository<OrderStatusLog, Long> {

    Set<OrderStatusLog> findByOrderId(Long orderId);

}
