package no.nav.aura.basta;

import no.nav.aura.basta.backend.fasit.deprecated.envconfig.client.PlatformTypeDO;
import no.nav.aura.basta.backend.fasit.deprecated.payload.PlatformType;
import no.nav.aura.basta.domain.input.vm.Converters;
import no.nav.aura.basta.domain.input.vm.NodeType;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConvertersTest {

    @Test
    public void platformTypeDOFromNodeTypeAndMiddleWareType() {
        MatcherAssert.assertThat(Converters.platformTypeDOFrom(NodeType.BPM_NODES), equalTo(PlatformTypeDO.BPM));
        MatcherAssert.assertThat(Converters.platformTypeDOFrom(NodeType.BPM86_NODES), equalTo(PlatformTypeDO.BPM86));
        MatcherAssert.assertThat(Converters.platformTypeDOFrom(NodeType.WAS_NODES), equalTo(PlatformTypeDO.WAS));
        MatcherAssert.assertThat(Converters.platformTypeDOFrom(NodeType.WAS9_NODES), equalTo(PlatformTypeDO.WAS9));
        MatcherAssert.assertThat(Converters.platformTypeDOFrom(NodeType.JBOSS), equalTo(PlatformTypeDO.JBOSS));
    }

    @Test
    public void illeagalNodeTypeConvertion() {
        assertThrows(IllegalArgumentException.class, () -> Converters.platformTypeDOFrom(NodeType.UNKNOWN));
    }

    @Test
    public void convertsFromBastaNodeTypeToFasitPlatformType() {
        MatcherAssert.assertThat(Converters.fasitPlatformTypeEnumFrom(NodeType.BPM_NODES), equalTo(PlatformType.BPM));
        MatcherAssert.assertThat(Converters.fasitPlatformTypeEnumFrom(NodeType.BPM86_NODES), equalTo(PlatformType.BPM86));
        MatcherAssert.assertThat(Converters.fasitPlatformTypeEnumFrom(NodeType.WAS_NODES), equalTo(PlatformType.WAS));
        MatcherAssert.assertThat(Converters.fasitPlatformTypeEnumFrom(NodeType.WAS9_NODES), equalTo(PlatformType.WAS9));
        MatcherAssert.assertThat(Converters.fasitPlatformTypeEnumFrom(NodeType.JBOSS), equalTo(PlatformType.JBOSS));
        MatcherAssert.assertThat(Converters.fasitPlatformTypeEnumFrom(NodeType.WILDFLY), equalTo(PlatformType.JBOSS));
    }

    @Test
    public void throwsExceptionForUnknownNodetype(){
        assertThrows(IllegalArgumentException.class, () -> Converters.fasitPlatformTypeEnumFrom(NodeType.UNKNOWN));
    }
}
