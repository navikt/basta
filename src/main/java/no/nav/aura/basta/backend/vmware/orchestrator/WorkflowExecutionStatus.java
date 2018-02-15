package no.nav.aura.basta.backend.vmware.orchestrator;

import no.nav.aura.basta.domain.input.vm.OrderStatus;

public enum WorkflowExecutionStatus {
    CANCELED(true),
    COMPLETED(false),
    RUNNING(false),
    SUSPENDED(true),
    WAITING(false),
    WAITING_SIGNAL(false),
    FAILED(true),
    INITIALIZING(false),
    UNKNOWN(true);

    private final boolean isFailedState;

    WorkflowExecutionStatus(boolean isFailedState) {
        this.isFailedState = isFailedState;
    }

    public boolean isFailedState() {
        return isFailedState;
    }

    public boolean isWaiting() {
        return this.equals(WAITING) || this.equals(WAITING_SIGNAL);
    }

    public static WorkflowExecutionStatus fromExecutionState(String state) {
        switch (state) {
            case "canceled":
                return CANCELED;
            case "completed":
                return COMPLETED;
            case "running":
                return RUNNING;
            case "suspended":
                return SUSPENDED;
            case "waiting":
                return WAITING;
            case "waiting-signal":
                return WAITING_SIGNAL;
            case "failed":
                return FAILED;
            case "initializing":
                return INITIALIZING;
            default:
                return UNKNOWN;
        }
    }
}
