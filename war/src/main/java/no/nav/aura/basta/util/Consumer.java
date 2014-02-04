package no.nav.aura.basta.util;

@SuppressWarnings("serial")
public abstract class Consumer<T> extends SerializableFunction<T, Boolean> {

    @Override
    public Boolean process(T t) {
        consume(t);
        return Boolean.TRUE;
    }

    public abstract void consume(T t);

}
