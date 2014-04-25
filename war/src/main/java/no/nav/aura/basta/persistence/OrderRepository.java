package no.nav.aura.basta.persistence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Long> {

    Order findByOrchestratorOrderId(String orchestratorOrderId);

    Iterable<Order> findByOrchestratorOrderIdNotNull();

    @Query("select o.id from Order o where (o.id < ?1 ) and rownum <= 1 order by o.id desc")
    Long findPreviousId(Long orderid);

    @Query("select o.id from Order o where (o.id > ?1 ) and rownum <= 1 order by o.id asc")
    Long findNextId(Long orderid);


}
