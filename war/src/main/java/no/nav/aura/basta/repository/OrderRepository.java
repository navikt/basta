package no.nav.aura.basta.repository;

import java.util.List;

import no.nav.aura.basta.domain.Order;

import org.joda.time.DateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrderRepository extends PagingAndSortingRepository<Order, Long> {

    Order findByExternalId(String orchestratorOrderId);

    @Query("select o from Order o where o.created >= ?1  and o.created <= ?2 and o.externalId IS NOT null  order by o.id desc")
    List<Order> findOrdersInTimespan(DateTime from, DateTime to, Pageable pageable);

    List<Order> findByExternalIdNotNullOrderByIdDesc(Pageable pageable);

    @Query("select o.id from Order o where (o.id < ?1 ) and rownum <= 1 and o.externalId IS NOT null order by o.id desc")
    Long findPreviousId(Long orderid);

    @Query("select o.id from Order o where (o.id > ?1 ) and rownum <= 1 and o.externalId IS NOT null order by o.id asc")
    Long findNextId(Long orderid);

    @Query("select o from Order o where o.status = 'WAITING'")
    List<Order> findWaitingOrders();

    @Query(value = "select o.* from ordertable o, result_properties r where r.result_value = ?1 and o.id = r.order_id", nativeQuery = true)
    List<Order> findRelatedOrders(String value);

}
