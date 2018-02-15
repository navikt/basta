package no.nav.aura.basta.security;

import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Guard {

    private static final Logger logger = LoggerFactory.getLogger(Guard.class);

    public static void checkSuperUserAccess() {
        User user = User.getCurrentUser();
        if (!user.hasSuperUserAccess()) {
            throw new UnauthorizedException("User " + user.getName() + " does not have super user access");
        }
        logger.info("User " + user.getName() + " has super user access");
    }


    public static void checkAccessToEnvironmentClass(final VMOrderInput input) {
        checkAccessToEnvironmentClass(input.getEnvironmentClass());
    }

    public static void checkAccessToEnvironmentClass(final EnvironmentClass environmentClass) {
        User user = User.getCurrentUser();
        if (!user.hasAccess(environmentClass)) {
            throw new UnauthorizedException("User " + user.getName() + " does not have access to environment class " + environmentClass);
        }
    }

}
