package no.nav.aura.basta.domain;

import no.nav.aura.basta.domain.vminput.NodeTypeInputResolver;
import no.nav.aura.basta.persistence.NodeType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class VMOrderInputTest {

    @Test
    public void testNodeTypeEquals() throws Exception {

        Input input = Input.single(NodeTypeInputResolver.NODE_TYPE, "PLAIN_LINUX");
        assertThat(NodeTypeInputResolver.getNodeType(input), is(equalTo(NodeType.PLAIN_LINUX)));
    }

    @Test
    public void testNodeTypeOptional() throws Exception {
        Input input = Input.single(NodeTypeInputResolver.NODE_TYPE, (String) null);
        assertThat(NodeTypeInputResolver.getNodeType(input), is(equalTo(NodeType.UNKNOWN)));
    }
}