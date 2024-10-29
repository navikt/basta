package no.nav.aura.basta.domain;

import no.nav.aura.basta.backend.fasit.payload.Zone;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DomainTest {
    @Test
    public void mappingFromEnvironmentAndZoneToDomainWorks() {
        Assertions.assertEquals(Domain.findBy(EnvironmentClass.u, Zone.fss), Domain.Devillo);
        Assertions.assertEquals(Domain.findBy(EnvironmentClass.u, Zone.sbs), Domain.DevilloSBS);
        Assertions.assertEquals(Domain.findBy(EnvironmentClass.t, Zone.fss), Domain.TestLocal);
        Assertions.assertEquals(Domain.findBy(EnvironmentClass.t, Zone.sbs), Domain.OeraT);
        Assertions.assertEquals(Domain.findBy(EnvironmentClass.q, Zone.fss), Domain.PreProd);
        Assertions.assertEquals(Domain.findBy(EnvironmentClass.q, Zone.sbs), Domain.OeraQ);
        Assertions.assertEquals(Domain.findBy(EnvironmentClass.p, Zone.fss), Domain.Adeo);
        Assertions.assertEquals(Domain.findBy(EnvironmentClass.p, Zone.sbs), Domain.Oera);

    }


}
