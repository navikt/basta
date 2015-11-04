package no.nav.aura.basta;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import no.nav.aura.basta.domain.input.vm.Converters;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.envconfig.client.PlatformTypeDO;

import org.junit.Test;

public class ConvertersTest {

    @Test
    public void platformTypeDOFromNodeTypeAndMiddleWareType() {
        assertThat(Converters.platformTypeDOFrom(NodeType.BPM_NODES), equalTo(PlatformTypeDO.BPM));
        assertThat(Converters.platformTypeDOFrom(NodeType.WAS_NODES), equalTo(PlatformTypeDO.WAS));
        assertThat(Converters.platformTypeDOFrom(NodeType.WAS_NODES), equalTo(PlatformTypeDO.WAS));
        assertThat(Converters.platformTypeDOFrom(NodeType.JBOSS), equalTo(PlatformTypeDO.JBOSS));
        assertThat(Converters.platformTypeDOFrom(NodeType.JBOSS), equalTo(PlatformTypeDO.JBOSS));
        assertThat(Converters.platformTypeDOFrom(NodeType.WILDFLY), equalTo(PlatformTypeDO.JBOSS));
        assertThat(Converters.platformTypeDOFrom(NodeType.OPENAM_SERVER), equalTo(PlatformTypeDO.OPENAM_SERVER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illeagalNodeTypeConvertion() {
        Converters.platformTypeDOFrom(NodeType.UNKNOWN);
    }

}
