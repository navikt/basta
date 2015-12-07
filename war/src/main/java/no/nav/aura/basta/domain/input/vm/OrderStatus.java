package no.nav.aura.basta.domain.input.vm;

import no.nav.aura.basta.rest.dataobjects.StatusLogLevel;

public enum OrderStatus {
    NEW(false, 0),
    PROCESSING(false, 1),
    WAITING(false, 2),
    SUCCESS(true, 3),
    WARNING(true, 4),
    FAILURE(true, 5),
    ERROR(true, 6);

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

    public static OrderStatus fromStatusLogLevel(StatusLogLevel option) {
        if (option == null) {
            return PROCESSING;
        }
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
