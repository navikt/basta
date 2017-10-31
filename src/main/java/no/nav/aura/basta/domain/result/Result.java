package no.nav.aura.basta.domain.result;


import java.util.List;
import java.util.Set;

import no.nav.aura.basta.rest.dataobjects.ResultDO;

public interface Result {

    List<String> keys();

    Set<ResultDO> asResultDO();

    String getDescription();
}
