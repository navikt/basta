package no.nav.aura.basta;

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
        //assertThat(Converters.platformTypeDOFrom(NodeType.BPM86_NODES), equalTo(PlatformTypeDO.BPM86));
        assertThat(Converters.platformTypeDOFrom(NodeType.WAS_NODES), equalTo(PlatformTypeDO.WAS));
        assertThat(Converters.platformTypeDOFrom(NodeType.WAS9_NODES), equalTo(PlatformTypeDO.WAS9));
        assertThat(Converters.platformTypeDOFrom(NodeType.LIBERTY), equalTo(PlatformTypeDO.LIBERTY));
        assertThat(Converters.platformTypeDOFrom(NodeType.JBOSS), equalTo(PlatformTypeDO.JBOSS));
        assertThat(Converters.platformTypeDOFrom(NodeType.OPENAM_SERVER), equalTo(PlatformTypeDO.OPENAM_SERVER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illeagalNodeTypeConvertion() {
        Converters.platformTypeDOFrom(NodeType.UNKNOWN);
    }

    @Test
    public void convertsFromBastaNodeTypeToFasitPlatformType() {
        assertThat(Converters.fasitPlatformTypeFrom(NodeType.BPM_NODES), equalTo("bpm"));
        assertThat(Converters.fasitPlatformTypeFrom(NodeType.BPM86_NODES), equalTo("bpm86"));
        assertThat(Converters.fasitPlatformTypeFrom(NodeType.WAS_NODES), equalTo("was"));
        assertThat(Converters.fasitPlatformTypeFrom(NodeType.WAS9_NODES), equalTo("was9"));
        assertThat(Converters.fasitPlatformTypeFrom(NodeType.LIBERTY), equalTo("liberty"));
        assertThat(Converters.fasitPlatformTypeFrom(NodeType.JBOSS), equalTo("jboss"));
        assertThat(Converters.fasitPlatformTypeFrom(NodeType.WILDFLY), equalTo("jboss"));
        assertThat(Converters.fasitPlatformTypeFrom(NodeType.OPENAM_SERVER), equalTo("openam_server"));
        assertThat(Converters.fasitPlatformTypeFrom(NodeType.DOCKERHOST), equalTo("docker"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionForUnknownNodetype(){
        Converters.fasitPlatformTypeFrom(NodeType.UNKNOWN);
    }
}
