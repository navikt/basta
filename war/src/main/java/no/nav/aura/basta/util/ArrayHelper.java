package no.nav.aura.basta.util;

public abstract class ArrayHelper {

    private ArrayHelper() {
    }

    @SafeVarargs
    public static <T> T[] of(T... ts) {
        return ts;
    }

}
