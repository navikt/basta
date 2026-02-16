package no.nav.aura.basta.rest;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.inject.Inject;
import no.nav.aura.basta.domain.SystemNotification;
import no.nav.aura.basta.repository.SystemNotificationRepository;
import no.nav.aura.basta.rest.dataobjects.SystemNotificationDO;

@Component
@RestController
@RequestMapping("/rest/system")
public class SystemRestService {

    @Inject
    SystemNotificationRepository systemNotificationRepository;

    @GetMapping("/notifications/{id}")
    public ResponseEntity<SystemNotificationDO> getNotification(@PathVariable Long id) {
        SystemNotification notification = systemNotificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entity not found " + id));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().noStore().mustRevalidate())
                .body(SystemNotificationDO.from(notification));
    }

    @PutMapping("/notifications/{id}/inactive")
    public ResponseEntity<Void> setInactive(@PathVariable Long id) {
        SystemNotification notification = systemNotificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entity not found " + id));
        notification.setInactive();
        systemNotificationRepository.save(notification);
        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noCache().noStore().mustRevalidate())
                .build();
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<SystemNotificationDO>> getSystemNotifications() {
        List<SystemNotificationDO> notifications = StreamSupport.stream(
                systemNotificationRepository.findAll().spliterator(), false)
                .map(SystemNotificationDO::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().noStore().mustRevalidate())
                .body(notifications);
    }

    @GetMapping("/notifications/active")
    public ResponseEntity<List<SystemNotificationDO>> getActiveSystemNotifications() {
        List<SystemNotificationDO> notifications = StreamSupport.stream(
                systemNotificationRepository.findByActiveTrue().spliterator(), false)
                .map(SystemNotificationDO::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().noStore().mustRevalidate())
                .body(notifications);
    }

    @PostMapping("/notifications/create/info")
    public ResponseEntity<Void> addSystemNotification(@RequestBody String message) {
        SystemNotification save = systemNotificationRepository.save(
                SystemNotification.newSystemNotification(message));
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/system/notifications/{id}")
                .buildAndExpand(save.getId())
                .toUri();
        return ResponseEntity.created(location)
                .cacheControl(CacheControl.noCache().noStore().mustRevalidate())
                .build();
    }

    @PostMapping("/notifications/create")
    public ResponseEntity<Void> addSystemNotification(@RequestBody Map<String, String> messagePayload) {
        return addSystemNotification(messagePayload.get("message"));
    }

    @PostMapping("/notifications/create/blocking")
    public ResponseEntity<Void> addBlockingSystemNotification(@RequestBody String message) {
        SystemNotification save = systemNotificationRepository.save(
                SystemNotification.newBlockingSystemNotification(message));
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/system/notifications/{id}")
                .buildAndExpand(save.getId())
                .toUri();
        return ResponseEntity.created(location)
                .cacheControl(CacheControl.noCache().noStore().mustRevalidate())
                .build();
    }
}