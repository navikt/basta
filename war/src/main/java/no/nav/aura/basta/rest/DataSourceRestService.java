package no.nav.aura.basta.rest;

import no.nav.aura.basta.spring.SpringConfig;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Component
@Path("/datasource")
public class DataSourceRestService {

    @Inject
    private SpringConfig springConfig;

    @GET
    @NoCache
    public String getCurrentUser() {
        return springConfig.getDataSourceConnection();
    }

}
