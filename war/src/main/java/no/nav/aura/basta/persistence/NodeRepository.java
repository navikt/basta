package no.nav.aura.basta.persistence;

import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface NodeRepository extends CrudRepository<Node, Long> {

    //NodeRS
    //@Query("select n from Node n where (n.order.createdBy = ?1 or ?1 is null) and (n.decommissionOrder is null or ?2 = true)")
    //Set<Node> findBy(String user, boolean includeDecommissioned);

    //Fasit update service
    //OrdersRS
    // Test
    @Query("select n from Node n where (n.hostname=?1 and (n.nodeStatus <> 'DECOMMISSIONED'))")
    Iterable<Node> findActiveNodesByHostname(String hostname);

    //TEST-USAGE

    Iterable<Node> findByHostname(String hostname);


}
