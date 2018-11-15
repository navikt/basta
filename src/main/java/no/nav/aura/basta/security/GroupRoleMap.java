package no.nav.aura.basta.security;

import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;

import java.util.*;

import static no.nav.aura.basta.security.ApplicationRole.*;

@Component
public class GroupRoleMap {

    private Map<String, Set<ApplicationRole>> groupRoleMap = new HashMap<>();

    private final static String operationsGroups = System.getProperty("basta_operations_groups");
    private final static String superUserGroups = System.getProperty("basta_superuser_groups");
    private final static String propOperationsGroups = System.getProperty("basta_prodoperations_groups");

    private GroupRoleMap() {
    }

    public Set<ApplicationRole> getRoles(String groupName) {
        return groupRoleMap.getOrDefault(groupName.toLowerCase(), Collections.EMPTY_SET);
    }

    public static GroupRoleMap builGroupRoleMapping() {
        GroupRoleMap groupRoleMapper = new GroupRoleMap();

        groupRoleMapper.addGroupRoleMapping(operationsGroups, ROLE_OPERATIONS);
        groupRoleMapper.addGroupRoleMapping(superUserGroups, ROLE_SUPERUSER);
        groupRoleMapper.addGroupRoleMapping(propOperationsGroups, ROLE_PROD_OPERATIONS);

        return groupRoleMapper;
    }

    /**
     * Creates a reverse Map on the format groupName=ApplicationRole1,ApplicationRole2 etc This makes it faster and easier
     * to compare with the group names (authorities) from LDAP passed in to the mapAuthorities method
     */

    private void addGroupRoleMapping(String groupString, ApplicationRole applicationRole) {

        Arrays.stream(groupString.split(","))
                .map(groupName -> groupName.trim().toLowerCase())
                .forEach(groupName -> {
                    if (!groupRoleMap.containsKey(groupName)) {
                        groupRoleMap.put(groupName, defaultRole());
                    }
                    groupRoleMap.get(groupName).add(applicationRole);
                });
    }

    private Set<ApplicationRole> defaultRole() {
        return Sets.newHashSet(ROLE_USER);
    }


}
