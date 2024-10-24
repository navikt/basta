package no.nav.aura.basta;

import no.nav.aura.basta.backend.fasit.payload.PlatformType;
import no.nav.aura.basta.domain.input.vm.Converters;
import no.nav.aura.basta.domain.input.vm.NodeType;
import no.nav.aura.envconfig.client.PlatformTypeDO;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ConvertersTest {

    @Test
    public void platformTypeDOFromNodeTypeAndMiddleWareType() {
        assertThat(Converters.platformTypeDOFrom(NodeType.BPM_NODES), equalTo(PlatformTypeDO.BPM));
        assertThat(Converters.platformTypeDOFrom(NodeType.BPM86_NODES), equalTo(PlatformTypeDO.BPM86));
        assertThat(Converters.platformTypeDOFrom(NodeType.WAS_NODES), equalTo(PlatformTypeDO.WAS));
        assertThat(Converters.platformTypeDOFrom(NodeType.WAS9_NODES), equalTo(PlatformTypeDO.WAS9));
        assertThat(Converters.platformTypeDOFrom(NodeType.JBOSS), equalTo(PlatformTypeDO.JBOSS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illeagalNodeTypeConvertion() {
        Converters.platformTypeDOFrom(NodeType.UNKNOWN);
    }

    @Test
    public void convertsFromBastaNodeTypeToFasitPlatformType() {
        assertThat(Converters.fasitPlatformTypeEnumFrom(NodeType.BPM_NODES), equalTo(PlatformType.BPM));
        assertThat(Converters.fasitPlatformTypeEnumFrom(NodeType.BPM86_NODES), equalTo(PlatformType.BPM86));
        assertThat(Converters.fasitPlatformTypeEnumFrom(NodeType.WAS_NODES), equalTo(PlatformType.WAS));
        assertThat(Converters.fasitPlatformTypeEnumFrom(NodeType.WAS9_NODES), equalTo(PlatformType.WAS9));
        assertThat(Converters.fasitPlatformTypeEnumFrom(NodeType.JBOSS), equalTo(PlatformType.JBOSS));
        assertThat(Converters.fasitPlatformTypeEnumFrom(NodeType.WILDFLY), equalTo(PlatformType.JBOSS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionForUnknownNodetype(){
        Converters.fasitPlatformTypeEnumFrom(NodeType.UNKNOWN);
    }
}
