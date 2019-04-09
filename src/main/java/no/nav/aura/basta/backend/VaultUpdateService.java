package no.nav.aura.basta.backend;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VaultUpdateService {
    private final Vault vault;

    public VaultUpdateService(Vault vault) {
        this.vault = vault;
    }

    public void writeSecrets(String path, Map<String, Object> resource) throws VaultException {
        vault.logical().write(path, resource);
    }
}
