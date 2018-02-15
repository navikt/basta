package no.nav.aura.basta;

import org.springframework.stereotype.Component;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@Component
@ApplicationPath("/rest/")
public class JaxrsApplication extends Application{
}
