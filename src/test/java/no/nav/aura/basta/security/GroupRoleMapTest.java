package no.nav.aura.basta.security;

import org.junit.jupiter.api.Test;

import static no.nav.aura.basta.security.ApplicationRole.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class GroupRoleMapTest {
    static final String GROUP1 = "group1";
    static final String ANOTHER_GROUP = "another_group";
    static final String GA_SOME_GROUP = "0000-GA_SOME_GROUP";

    @Test
    public void testRoleMapping() {
        final String operationGroups = "group1,another_group";
        final String superuserGroups = "group1,0000-GA_SOME_GROUP";
        final String prodOperationGroups = "group1,another_group";

        GroupRoleMap actual = GroupRoleMap.builGroupRoleMapping(operationGroups, superuserGroups, prodOperationGroups);
        assertThat(actual.getRoles(GROUP1), containsInAnyOrder(ROLE_USER, ROLE_OPERATIONS, ROLE_SUPERUSER, ROLE_PROD_OPERATIONS));
        assertThat(actual.getRoles(ANOTHER_GROUP), containsInAnyOrder(ROLE_USER, ROLE_OPERATIONS, ROLE_PROD_OPERATIONS));
        assertThat(actual.getRoles(GA_SOME_GROUP), containsInAnyOrder(ROLE_USER, ROLE_SUPERUSER));

        System.out.println(actual);
    }
}
