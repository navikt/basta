package no.nav.aura.basta.domain.input;

import java.util.Map;

public interface Input {

       String getOrderDescription();

    Map<String, String> copy();
}
