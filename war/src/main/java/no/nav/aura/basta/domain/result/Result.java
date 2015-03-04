package no.nav.aura.basta.domain.result;


import no.nav.aura.basta.rest.dataobjects.ResultDO;

import java.util.List;
import java.util.TreeSet;

public interface Result {

    List<String> keys();

    TreeSet<ResultDO> asResultDO();

    String getDescription();
}
