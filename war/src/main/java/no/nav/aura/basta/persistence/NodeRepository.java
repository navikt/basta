package no.nav.aura.basta.persistence;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;

public interface NodeRepository extends CrudRepository<Node, Long> {

    Set<Node> findByOrderId(Long orderId);

}
