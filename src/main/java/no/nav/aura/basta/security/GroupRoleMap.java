package no.nav.aura.basta.security;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static no.nav.aura.basta.security.ApplicationRole.*;

@Component
public class GroupRoleMap {

    private Map<String, Set<ApplicationRole>> groupRoleMap = new HashMap<>();

    private final static String operationsGroups = System.getProperty("BASTA_OPERATIONS_GROUPS");
    private final static String superUserGroups = System.getProperty("BASTA_SUPERUSER_GROUPS");
    private final static String propOperationsGroups = System.getProperty("BASTA_PRODOPERATIONS_GROUPS");
    private final static Set<ApplicationRole> defaultRole = Sets.newHashSet(ROLE_USER);

    private static final Logger log = LoggerFactory.getLogger(GroupRoleMap.class);

    private GroupRoleMap() {
    }

    public Set<ApplicationRole> getRoles(String groupName) {
        return groupRoleMap.getOrDefault(groupName.toLowerCase(), defaultRole);
    }

    @Override
    public String toString() {
        return groupRoleMap
                .keySet()
                .stream()
                .map(k -> k + ": " + groupRoleMap.get(k).stream().map(role -> role.toString()).collect(joining(", ")))
                .collect(joining("\n"));
    }

    public static GroupRoleMap builGroupRoleMapping() {
        log.info("Building grm");
        GroupRoleMap groupRoleMapper = new GroupRoleMap();

        groupRoleMapper.addGroupRoleMapping(operationsGroups, ROLE_OPERATIONS);
        groupRoleMapper.addGroupRoleMapping(superUserGroups, ROLE_SUPERUSER);
        groupRoleMapper.addGroupRoleMapping(propOperationsGroups, ROLE_PROD_OPERATIONS);

        log.info("Finished building groupRoleMap\n" + groupRoleMapper.toString());

        return groupRoleMapper;
    }

    /**
     * Creates a reverse Map on the format groupName=ApplicationRole1,ApplicationRole2 etc This makes it faster and easier
     * to compare with the group names (authorities) from LDAP passed in to the mapAuthorities method
     */

    private void addGroupRoleMapping(String groupString, ApplicationRole applicationRole) {

        if (groupString == null) {
            return;
        }

        Arrays.stream(groupString.split(","))
                .map(groupName -> groupName.trim().toLowerCase())
                .forEach(groupName -> {
                    if (!groupRoleMap.containsKey(groupName)) {
                        groupRoleMap.put(groupName, Sets.newHashSet(ROLE_USER));
                    }
                    groupRoleMap.get(groupName).add(applicationRole);
                });
    }
}
