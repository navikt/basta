package no.nav.aura.basta.domain.input.vm;

import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;

public enum OrderStatus {
    NEW(false, 0),
    PROCESSING(false, 1),
    SUCCESS(true, 2),
    WARNING(true, 3),
    FAILURE(true, 4),
    ERROR(true, 5);

    private final boolean endstate;
    private final int priority;

    OrderStatus(boolean endstate, int priority) {
        this.endstate = endstate;
        this.priority = priority;
    }

    public boolean isEndstate() {
        return endstate;
    }

    public boolean isMoreImportantThan(OrderStatus status) {
        return this.priority >= status.priority;
    }

    public static OrderStatus fromString(StatusLogLevel option) {
        switch (option) {
        case error:
            return ERROR;
        case success:
            return SUCCESS;
        case warning:
            return WARNING;
        default:
            return PROCESSING;
        }
    }
}
