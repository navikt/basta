package no.nav.aura.basta.security;

import java.util.*;

import no.nav.aura.basta.ApplicationRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class AuthoritiesMapper implements GrantedAuthoritiesMapper {

    private static final Logger log = LoggerFactory.getLogger(AuthoritiesMapper.class);
    private Map<String, Set<ApplicationRole>> groupRoleMap;

    public AuthoritiesMapper() {
        groupRoleMap = Maps.newHashMap();
    }

    private void initGroupRoleMapping() {
        Set<ApplicationRole> applicationRoles = EnumSet.allOf(ApplicationRole.class);

        for (ApplicationRole applicationRole : applicationRoles) {
            // Get a comma separated list of LDAP group names that have the current role from the runtime container
            String groupString = System.getProperty(applicationRole.name() + ".groups");
            if (groupString != null) {
                log.debug(String.format("Application role %s is mapped to the following LDAP groups %s", applicationRole.name(), groupString));
                addGroupRoleMapping(groupString, applicationRole);
            }
        }
    }

    /**
     * Creates a reverse Map on the format ldapGroupName=ApplicationRole1,ApplicationRole2 etc This makes it faster and easier
     * to compare with the group names (authorities) from LDAP passed in to the mapAuthorities method
     * */
    private void addGroupRoleMapping(String groupString, ApplicationRole applicationRole) {
        for (String ldapGroup : Arrays.asList(groupString.split(","))) {
            String ldapGroupName = ldapGroup.trim().toLowerCase();
            if (groupRoleMap.get(ldapGroupName) != null) {
                groupRoleMap.get(ldapGroupName).add(applicationRole);
            } else {
                groupRoleMap.put(ldapGroupName, Sets.newHashSet(applicationRole));
            }
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> userGroups) {
        Set<GrantedAuthority> roles = Sets.newHashSet();
        initGroupRoleMapping();

        roles.add(ApplicationRole.ROLE_USER);
        for (GrantedAuthority group : userGroups) {
            String groupName = group.getAuthority().toLowerCase();
            if (groupRoleMap.containsKey(groupName)) {
                roles.addAll(groupRoleMap.get(groupName));
            }
        }

        return roles;
    }
}