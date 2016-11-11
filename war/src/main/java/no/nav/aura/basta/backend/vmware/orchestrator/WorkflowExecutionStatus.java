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

//    public static OrderStatus orderStatusFrom(WorkflowExecutionStatus status) {
//
//        switch (status) {
//            case INITIALIZING:
//            case RUNNING:
//                return OrderStatus.PROCESSING;
//            case WAITING:
//            case WAITING_SIGNAL:
//                return OrderStatus.WAITING;
//            case CANCELED:
//            case SUSPENDED:
//            case FAILED:
//                return OrderStatus.FAILURE;
//            case COMPLETED:
//            default:
//                return OrderStatus.SUCCESS;
//        }
//    }

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
