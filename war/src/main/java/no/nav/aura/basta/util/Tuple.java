package no.nav.aura.basta.util;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@SuppressWarnings("serial")
public class Tuple<F, S> implements Serializable {

    public final F fst;
    public final S snd;

    public Tuple(F fst, S snd) {
        this.fst = fst;
        this.snd = snd;
    }

    public static <F, S> Tuple<F, S> of(F fst, S snd) {
        return new Tuple<F, S>(fst, snd);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, false);
    }

    @Override
    public String toString() {
        return "Tuple(" + fst + ", " + snd + ")";
    }

}
