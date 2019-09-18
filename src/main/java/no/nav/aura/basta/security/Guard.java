package no.nav.aura.basta.security;

import no.nav.aura.basta.domain.input.EnvironmentClass;
import no.nav.aura.basta.domain.input.vm.VMOrderInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotAuthorizedException;

import static java.util.stream.Collectors.joining;

public class Guard {

    private static final Logger logger = LoggerFactory.getLogger(Guard.class);

    public static void checkSuperUserAccess() {
        User user = User.getCurrentUser();
        if (!user.hasSuperUserAccess()) {
            throw new NotAuthorizedException("User " + user.getName() + " does not have super user access");
        }
        logger.info("User " + user.getName() + " has super user access");
    }


    public static void checkAccessToEnvironmentClass(final VMOrderInput input) {
        checkAccessToEnvironmentClass(input.getEnvironmentClass());
    }

    public static void checkAccessToEnvironmentClass(final EnvironmentClass environmentClass) {
        User user = User.getCurrentUser();
        logger.info("User: " + user.getName() + " roles: " + user.getRoles().stream().collect(joining(",")));

        if (!user.hasAccess(environmentClass)) {
            throw new NotAuthorizedException("User " + user.getName() + " does not have access to environment class " +
                    environmentClass);
        }
    }

}
