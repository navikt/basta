package no.nav.aura.basta.repository;

import no.nav.aura.basta.domain.Order;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface OrderRepository extends PagingAndSortingRepository<Order, Long>, CrudRepository<Order, Long> {

    Order findByExternalId(String orchestratorOrderId);

    @Query("select o from Order o where o.externalId IS NOT null  order by o.id desc")
    Page<Order> findOrders(Pageable pageable);

    @Query("select o from Order o")
    @Cacheable(value = "orders")
    List<Order> getAllOrders();

    List<Order> findByExternalIdNotNullOrderByIdDesc(Pageable pageable);

    @Query("select o from Order o where o.status = 'WAITING'")
    List<Order> findWaitingOrders();

    @Query(value = "select o from Order o where o.status = 'PROCESSING' or o.status = 'NEW' and o.orderType = 'VM'")
    List<Order> findIncompleteVmOrders();

    @CachePut(value="orders", key="#s.id")
    @Override
    <S extends Order> S save(S s);
}
