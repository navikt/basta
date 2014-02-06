package no.nav.aura.basta.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import no.nav.aura.basta.persistence.Node;
import no.nav.aura.basta.persistence.NodeRepository;
import no.nav.aura.basta.util.SerializableFunction;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.stereotype.Component;

import com.google.common.collect.FluentIterable;

@Component
@Path("/nodes")
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodesRestService {

    @Inject
    private NodeRepository nodeRepository;

    @SuppressWarnings("serial")
    @GET
    public List<NodeDO> getNodes(@Context final UriInfo uriInfo, @QueryParam("user") String user, @QueryParam("includeDecommissioned") boolean includeDecommissioned) {
        Iterable<Node> nodes = nodeRepository.findBy(user, includeDecommissioned);
        return FluentIterable.from(nodes).transform(new SerializableFunction<Node, NodeDO>() {
            public NodeDO process(Node node) {
                return new NodeDO(node, uriInfo);
            }
        }).toList();
    }
}
