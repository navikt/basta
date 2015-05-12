package no.nav.aura.basta.backend.vmware.orchestrator.request;


// Need to have a common Request interface that both provison and decommission can inherit from so that WorkflowExectutor can remain generic
public interface OrchestatorRequest {

	OrchestatorRequest censore();
}
