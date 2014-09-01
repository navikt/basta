package no.nav.aura.basta.persistence;

import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface NodeRepository extends CrudRepository<Node, Long> {


    @Query("select n from Node n where (n.hostname=?1 and (n.nodeStatus <> 'DECOMMISSIONED'))")
    Iterable<Node> findActiveNodesByHostname(String hostname);

    //TEST-USAGE

    Iterable<Node> findByHostname(String hostname);


}
