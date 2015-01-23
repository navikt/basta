package no.nav.aura.basta.repository;

import no.nav.aura.basta.domain.SystemNotification;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface SystemNotificationRepository extends CrudRepository<SystemNotification, Long> {

    List<SystemNotification> findByActiveTrue();


}
