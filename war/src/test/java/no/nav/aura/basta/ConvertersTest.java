package no.nav.aura.basta;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import no.nav.aura.basta.domain.input.vm.Converters;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.envconfig.client.PlatformTypeDO;

public class ConvertersTest {

    @Test
    public void platformTypeDOFromNodeTypeAndMiddleWareType() {
        assertThat(Converters.platformTypeDOFrom(NodeType.BPM_NODES), equalTo(PlatformTypeDO.BPM));
        assertThat(Converters.platformTypeDOFrom(NodeType.BPM9_NODES), equalTo(PlatformTypeDO.BPM9));
        assertThat(Converters.platformTypeDOFrom(NodeType.WAS_NODES), equalTo(PlatformTypeDO.WAS));
        assertThat(Converters.platformTypeDOFrom(NodeType.WAS_NODES), equalTo(PlatformTypeDO.WAS));
        assertThat(Converters.platformTypeDOFrom(NodeType.WAS9_NODES), equalTo(PlatformTypeDO.WAS9));

        assertThat(Converters.platformTypeDOFrom(NodeType.LIBERTY), equalTo(PlatformTypeDO.LIBERTY));
        assertThat(Converters.platformTypeDOFrom(NodeType.JBOSS), equalTo(PlatformTypeDO.JBOSS));
        assertThat(Converters.platformTypeDOFrom(NodeType.JBOSS), equalTo(PlatformTypeDO.JBOSS));
        assertThat(Converters.platformTypeDOFrom(NodeType.OPENAM_SERVER), equalTo(PlatformTypeDO.OPENAM_SERVER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illeagalNodeTypeConvertion() {
        Converters.platformTypeDOFrom(NodeType.UNKNOWN);
    }

}
