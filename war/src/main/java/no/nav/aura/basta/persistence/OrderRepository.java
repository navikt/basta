package no.nav.aura.basta.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Set;

public interface OrderRepository extends PagingAndSortingRepository<Order, Long> {

    Order findByOrchestratorOrderId(String orchestratorOrderId);


    Set<Order> findByOrchestratorOrderIdNotNullOrderByIdDesc();

    List<Order> findByOrchestratorOrderIdNotNullOrderByIdDesc(Pageable pageable);

    @Query("select o.id from Order o where (o.id < ?1 ) and rownum <= 1 order by o.id desc")
    Long findPreviousId(Long orderid);

    @Query("select o.id from Order o where (o.id > ?1 ) and rownum <= 1 order by o.id asc")
    Long findNextId(Long orderid);


}
