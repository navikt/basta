package no.nav.aura.basta.persistence;

import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Long> {

    Order findByOrchestratorOrderId(String orchestratorOrderId);

}
