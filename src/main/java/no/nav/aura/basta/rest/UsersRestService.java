package no.nav.aura.basta.rest;

import no.nav.aura.basta.rest.dataobjects.UserDO;
import no.nav.aura.basta.security.User;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
@RequestMapping("/rest/users")
public class UsersRestService {

    @GetMapping("/current")
    public ResponseEntity<UserDO> getCurrentUser() {
        User user = User.getCurrentUser();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().noStore().mustRevalidate())
                .body(new UserDO(user));
    }

}