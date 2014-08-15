package no.nav.aura.basta.rest;

import com.google.common.collect.FluentIterable;
import no.nav.aura.basta.persistence.Node;
import no.nav.aura.basta.persistence.NodeRepository;
import no.nav.aura.basta.persistence.Order;
import no.nav.aura.basta.security.Guard;
import no.nav.aura.basta.util.SerializableFunction;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Component
@Path("/fasitintegration")
@Transactional
public class FasitIntegrationRestService {
    private static final Logger logger = LoggerFactory.getLogger(FasitIntegrationRestService.class);



}
