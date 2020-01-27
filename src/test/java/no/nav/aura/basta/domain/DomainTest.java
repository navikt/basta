package no.nav.aura.basta.domain;

import no.nav.aura.basta.backend.fasit.payload.Zone;
import no.nav.aura.basta.domain.input.Domain;
import no.nav.aura.basta.domain.input.EnvironmentClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DomainTest {
    @Test
    public void mappingFromEnvironmentAndZoneToDomainWorks() {
        assertEquals(Domain.findBy(EnvironmentClass.u, Zone.fss), Domain.Devillo);
        assertEquals(Domain.findBy(EnvironmentClass.u, Zone.sbs), Domain.DevilloSBS);
        assertEquals(Domain.findBy(EnvironmentClass.t, Zone.fss), Domain.TestLocal);
        assertEquals(Domain.findBy(EnvironmentClass.t, Zone.sbs), Domain.OeraT);
        assertEquals(Domain.findBy(EnvironmentClass.q, Zone.fss), Domain.PreProd);
        assertEquals(Domain.findBy(EnvironmentClass.q, Zone.sbs), Domain.OeraQ);
        assertEquals(Domain.findBy(EnvironmentClass.p, Zone.fss), Domain.Adeo);
        assertEquals(Domain.findBy(EnvironmentClass.p, Zone.sbs), Domain.Oera);

    }


}
