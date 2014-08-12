package no.nav.aura.basta.security;


import no.nav.aura.basta.User;
import no.nav.aura.basta.persistence.EnvironmentClass;
import no.nav.aura.basta.persistence.NodeType;
import no.nav.aura.basta.rest.OrderDetailsDO;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Guard {

    private static final Logger logger = LoggerFactory.getLogger(Guard.class);

    public static void checkUserAccess(final OrderDetailsDO orderDetails) {
        checkAccessToEnvironmentClass(orderDetails.getEnvironmentClass());
        checkAccessToPlainLinux(orderDetails.getNodeType());
    }

    public static void checkAccessAllowedFromRemoteAddress(String remoteAddr) {
        // TODO Check remote address
        logger.info("Called from " + remoteAddr);
    }

    public static void checkSuperUserAccess() {
        User user = User.getCurrentUser();
        if (!user.hasSuperUserAccess()) {
            throw new UnauthorizedException("User " + user.getName() + " does not have super user access");
        }
        logger.info("User " + user.getName() + " has super user access");
    }

    private static void checkAccessToPlainLinux(final NodeType nodeType) {
        User user = User.getCurrentUser();
        if (!user.hasSuperUserAccess() && nodeType.equals(NodeType.PLAIN_LINUX)) {
            throw new UnauthorizedException("User " + user.getName() + " does not have access to order a plain linux server");
        }
    }

    private static void checkAccessToEnvironmentClass(final EnvironmentClass environmentClass) {
        User user = User.getCurrentUser();
        if (!user.hasAccess(environmentClass)) {
            throw new UnauthorizedException("User " + user.getName() + " does not have access to environment class " + environmentClass);
        }
    }


}
