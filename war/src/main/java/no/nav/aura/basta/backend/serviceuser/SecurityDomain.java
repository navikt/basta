package no.nav.aura.basta.backend.serviceuser;

import static no.nav.aura.basta.domain.input.Domain.Devillo;
import static no.nav.aura.basta.domain.input.Domain.DevilloSBS;
import static no.nav.aura.basta.domain.input.Domain.Oera;
import static no.nav.aura.basta.domain.input.Domain.OeraQ;
import static no.nav.aura.basta.domain.input.Domain.OeraT;

import java.util.Arrays;
import java.util.List;

import no.nav.aura.basta.domain.input.Domain;

public enum SecurityDomain {
    SEC_TestLocal("test.local", Devillo, DevilloSBS, Domain.TestLocal, OeraT),
    SEC_PreProd("preprod.local", Domain.PreProd, OeraQ),
    SEC_Adeo("adeo.no", Domain.Adeo, Oera);
    
    private String fqdn;
    private List<Domain> domains;

    private SecurityDomain(String fqdn, Domain... domains) {
        this.fqdn = fqdn;
        this.domains = Arrays.asList(domains);

    }
    
    
    public static String forDomain(Domain domain){
        for (SecurityDomain sd : values()) {
            if (sd.domains.contains(domain)){
                return sd.fqdn;
            }
        }
        throw new IllegalArgumentException("Unknown security domain for " + domain);
        
    }

}
