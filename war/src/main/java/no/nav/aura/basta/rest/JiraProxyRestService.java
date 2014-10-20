package no.nav.aura.basta.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("/jiraproxy")
@Transactional
public class JiraProxyRestService {

    private static final Logger log = LoggerFactory.getLogger(JiraProxyRestService.class);
    private final String JIRA_BASE_URL = "http://jira.adeo.no/";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response performJiraRestQuery(@QueryParam("path") String path) {
        ClientRequest request = new ClientRequest(JIRA_BASE_URL + path);
        try {
            ClientResponse<String> response = request.get(String.class);
            return Response.ok(response.getEntity()).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
