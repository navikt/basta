package no.nav.aura.basta.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface SystemNotificationRepository extends CrudRepository<SystemNotification, Long> {

    List<SystemNotification> findByActiveTrue();


}
