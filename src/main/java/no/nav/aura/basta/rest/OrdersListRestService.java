package no.nav.aura.basta.rest;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.inject.Inject;
import no.nav.aura.basta.domain.MapOperations;
import no.nav.aura.basta.domain.Order;
import no.nav.aura.basta.domain.OrderStatusLog;
import no.nav.aura.basta.repository.OrderRepository;
import no.nav.aura.basta.rest.dataobjects.OrderStatusLogDO;
import no.nav.aura.basta.rest.dataobjects.ResultDO;
import no.nav.aura.basta.rest.vm.dataobjects.OrderDO;

@Component
@RestController
@RequestMapping("/rest/orders")
@Transactional
public class OrdersListRestService {
    private OrderRepository orderRepository;

    @Inject
    public OrdersListRestService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/page/{page}/{size}")
    public ResponseEntity<List<OrderDO>> getOrdersInPages(@PathVariable int page, @PathVariable int size) {
        Page<Order> orders = orderRepository.findOrders(PageRequest.of(page, size));
        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            List<OrderDO> orderDos = orders.stream().map(order -> new OrderDO(order)).collect(toList());
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                    .header("total_count", String.valueOf(orders.getTotalElements()))
                    .header("page_count", String.valueOf(orders.getTotalPages()))
                    .body(orderDos);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDO> getOrder(@PathVariable long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        OrderDO orderDO = createRichOrderDO(order);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore().mustRevalidate())
                .body(orderDO);
    }

    @GetMapping("/{orderid}/statuslog")
    public ResponseEntity<List<OrderStatusLogDO>> getStatusLog(@PathVariable long orderid) {
        Order order = orderRepository.findById(orderid).orElse(null);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<OrderStatusLog> orderStatusLogs = order.getStatusLogs();
        List<OrderStatusLogDO> logs = orderStatusLogs.stream()
                .map(OrderStatusLogDO::new)
                .collect(toList());

        CacheControl.noCache();
		return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore().mustRevalidate())
                .body(logs);
    }

    protected OrderDO createRichOrderDO(Order order) {
        OrderDO orderDO = new OrderDO(order);
        
        // Set the full URI for this order using Spring's ServletUriComponentsBuilder
        URI orderUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/rest/orders/{id}")
                .buildAndExpand(order.getId())
                .toUri();
        orderDO.setUri(orderUri);
        orderDO.setInput(order.getInputAs(MapOperations.class).copy());

        for (ResultDO result : order.getResult().asResultDO()) {
            orderDO.addResultHistory(result);
        }
        return orderDO;
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}