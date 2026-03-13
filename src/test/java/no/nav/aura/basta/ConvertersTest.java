package no.nav.aura.basta;

import no.nav.aura.basta.backend.fasit.rest.model.infrastructure.PlatformType;
import no.nav.aura.basta.domain.input.vm.Converters;
import no.nav.aura.basta.domain.input.vm.NodeType;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConvertersTest {

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
