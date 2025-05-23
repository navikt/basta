package no.nav.aura.basta.util;

import com.google.common.base.Function;

import java.io.Serializable;

public abstract class SerializableFunction<F, T> implements Function<F, T>, Serializable {

    /** Overriding nullable method to avoid trouble with checkstyle */
    public final T apply(F input) {
        return process(input);
    }

    public abstract T process(F input);
}
