package no.nav.aura.basta.security;

import org.junit.Test;

import static no.nav.aura.basta.security.ApplicationRole.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class GroupRoleMapTest {
    static final String GROUP1 = "group1";
    static final String ANOTHER_GROUP = "another_group";
    static final String GA_SOME_GROUP = "0000-GA_SOME_GROUP";

    @Test
    public void testRoleMapping() {
        System.setProperty("BASTA_OPERATIONS_GROUPS", "group1,another_group");
        System.setProperty("BASTA_SUPERUSER_GROUPS", "group1,0000-GA_SOME_GROUP");
        System.setProperty("BASTA_PRODOPERATIONS_GROUPS","group1,another_group");

        GroupRoleMap actual = GroupRoleMap.builGroupRoleMapping();
        assertThat(actual.getRoles(GROUP1), containsInAnyOrder(ROLE_USER, ROLE_OPERATIONS, ROLE_SUPERUSER, ROLE_PROD_OPERATIONS));
        assertThat(actual.getRoles(ANOTHER_GROUP), containsInAnyOrder(ROLE_USER, ROLE_OPERATIONS, ROLE_PROD_OPERATIONS));
        assertThat(actual.getRoles(GA_SOME_GROUP), containsInAnyOrder(ROLE_USER, ROLE_SUPERUSER));

        System.out.println(actual.toString());
    }
}
