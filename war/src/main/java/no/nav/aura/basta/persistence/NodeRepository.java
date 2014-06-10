package no.nav.aura.basta.persistence;

import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface NodeRepository extends CrudRepository<Node, Long> {

    Set<Node> findByOrder(Order order);
    Set<Node> findByDecommissionOrder(Order order);

    Set<Node> findByOrderCreatedBy(String user);

    @Query("select n from Node n where (n.order.createdBy = ?1 or ?1 is null) and (n.decommissionOrder is null or ?2 = true)")
    Set<Node> findBy(String user, boolean includeDecommissioned);

    Iterable<Node> findByHostnameAndDecommissionOrderIdIsNull(String hostname);

    Iterable<Node> findByHostname(String hostname);


}
