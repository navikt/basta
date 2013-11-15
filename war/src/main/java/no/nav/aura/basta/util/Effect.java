package no.nav.aura.basta.util;

@SuppressWarnings("serial")
public abstract class Effect extends SerializableFunction<Void, Void> {

    public abstract void perform();

    public final Void process(Void v) {
        perform();
        return null;
    }
}
