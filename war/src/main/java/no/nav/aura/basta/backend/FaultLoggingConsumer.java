package no.nav.aura.basta.backend;

import no.nav.aura.basta.util.Consumer;

import org.slf4j.Logger;

@SuppressWarnings("serial")
public abstract class FaultLoggingConsumer<T> extends Consumer<T> {

    private String errorMessage;
    private Logger logger;

    public FaultLoggingConsumer(Logger logger, String errorMessage) {
        this.logger = logger;
        this.errorMessage = errorMessage;
    }

    @Override
    public final void consume(T t) {
        try {
            consumeAndLogFaults(t);
        } catch (RuntimeException e) {
            logger.error(errorMessage, e);
        }
    }

    public abstract void consumeAndLogFaults(T t);

}
