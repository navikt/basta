package no.nav.aura.basta.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import no.nav.aura.basta.EnvironmentClass;
import no.nav.aura.basta.User;
import no.nav.aura.basta.order.OrderV1Factory;
import no.nav.aura.basta.vmware.XmlUtils;
import no.nav.aura.basta.vmware.orchestrator.requestv1.ProvisionRequest;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.springframework.stereotype.Component;

@Component
@Path("/")
public class OrdersRestService {

    @POST
    @Path("/orders")
    @Consumes(MediaType.APPLICATION_JSON)
    public String postOrder(SettingsDO settings, @QueryParam("dryRun") Boolean dryRun) {
        checkAccess(EnvironmentClass.from(settings.getEnvironmentClass()));
        ProvisionRequest order = new OrderV1Factory(settings).createOrder();
        try {
            return XmlUtils.prettyFormat(XmlUtils.generateXml(order), 2);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkAccess(EnvironmentClass environmentClass) {
        User user = User.getCurrentUser();
        if (!user.getEnvironmentClasses().contains(environmentClass)) {
            throw new UnauthorizedException("User " + user.getName() + " does not have access to environment class " + environmentClass);
        }
    }
}
