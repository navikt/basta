package no.nav.aura.basta.rest;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.LookupResponse;

import jakarta.inject.Inject;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(path = "/rest/internal")
public class InternalRestService {
    @Inject
    private Vault vault;

    @GetMapping(path = "/isAlive", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> isAlive() {
		return ResponseEntity.ok().build();
	}

    @GetMapping(path = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> health() throws VaultException {
		LookupResponse self = vault.auth().lookupSelf();
		return ResponseEntity.ok(self.getTTL());
	}
}
