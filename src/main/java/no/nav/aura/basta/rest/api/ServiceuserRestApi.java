package no.nav.aura.basta.rest.api;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.inject.Inject;
import no.nav.aura.basta.backend.serviceuser.FasitServiceUserAccount;
import no.nav.aura.basta.rest.serviceuser.ServiceUserCredentialOperationRestService;

@Component
@RestController
@RequestMapping("/rest/api/orders/serviceuser")
@Transactional
public class ServiceuserRestApi {

    private static final Logger logger = LoggerFactory.getLogger(ServiceuserRestApi.class);

    @Inject
    private ServiceUserCredentialOperationRestService serviceUserCredentialOperationRestService;

    @PostMapping("/stop")
    public ResponseEntity<?> stopCredential(@RequestBody Map<String, String> input) {
        Map<String, String> params = null;
        try {
            params = validateAndEnrichInput(input);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
        logger.info("Stopping credential with params {}", params);
        return serviceUserCredentialOperationRestService.stopServiceUserCredential(params);
    }

    @PostMapping("/remove")
    public ResponseEntity<?> deleteCredential(@RequestBody Map<String, String> input) {
        Map<String, String> params = null;
        try {
            params = validateAndEnrichInput(input);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
        logger.info("Deleting credential with params {}", params);
        try {
            ResponseEntity<?> response = serviceUserCredentialOperationRestService.deleteServiceUser(params);
            return response;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input parameter: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private Map<String, String> validateAndEnrichInput(Map<String, String> input) {
        if (!input.containsKey("environmentClass")) {
            throw new IllegalArgumentException("Missing required inputparameter environmentClass");
        }
        if (!input.containsKey("zone")) {
            throw new IllegalArgumentException("Missing required inputparameter zone");
        }

        if (!input.containsKey("application") && !input.containsKey("fasitAlias")) {
            throw new IllegalArgumentException("Input must contain parameter application or fasitAlias");
        }

        if (input.containsKey("fasitAlias")) {
            String alias = input.get("fasitAlias");
            logger.info("Parameter application not found. Using parameter fasitAlias {} to find application name", alias);
            input.put("application", FasitServiceUserAccount.getApplicationNameFromAlias(alias));
        }

        return input;
    }

}