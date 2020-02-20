package no.nav.aura.basta.repository;

import no.nav.aura.basta.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;

public interface OrderRepository extends PagingAndSortingRepository<Order, Long> {

    Order findByExternalId(String orchestratorOrderId);

    @Query("select o from Order o where o.externalId IS NOT null  order by o.id desc")
    Page<Order> findOrders(Pageable pageable);

    @Query("select o from Order o order by o.id desc")
    List<Order> getAllOrders();

    List<Order> findByExternalIdNotNullOrderByIdDesc(Pageable pageable);

    @Query("select o.id from Order o where (o.id < ?1 ) and rownum <= 1 and o.externalId IS NOT null order by o.id desc")
    Long findPreviousId(Long orderid);

    @Query("select o.id from Order o where (o.id > ?1 ) and rownum <= 1 and o.externalId IS NOT null order by o.id asc")
    Long findNextId(Long orderid);

    @Query("select o from Order o where o.status = 'WAITING'")
    List<Order> findWaitingOrders();

    @Query(value = "select o.* from ordertable o, result_properties r where r.result_value = ?1 and o.id = r.order_id" +
            " order by o.id desc", nativeQuery = true)
    List<Order> findRelatedOrders(String value);

    @Query(value = "select o from Order o where o.status = 'PROCESSING' or o.status = 'NEW' and o.orderType = 'VM'")
    List<Order> findIncompleteVmOrders();
}
