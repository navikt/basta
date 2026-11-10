package no.nav.aura.basta.util;

import java.io.Serializable;
import java.util.function.Function;

public abstract class SerializableFunction<F, T> implements Function<F, T>, Serializable {

    public final T apply(F input) {
        return process(input);
    }

    public abstract T process(F input);
}
