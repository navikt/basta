package no.nav.aura.basta.persistence;

import org.springframework.data.repository.CrudRepository;

public interface SettingsRepository extends CrudRepository<Settings, Long> {

    Settings findByOrderId(Long orderId);

}
