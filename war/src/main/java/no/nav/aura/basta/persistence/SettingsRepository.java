package no.nav.aura.basta.persistence;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;

public interface SettingsRepository extends CrudRepository<Settings, Long> {

    Set<Settings> findByOrderId(Long orderId);

}
