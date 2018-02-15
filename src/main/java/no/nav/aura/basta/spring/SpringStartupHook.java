package no.nav.aura.basta.spring;

import javax.inject.Inject;

import no.nav.aura.basta.backend.WaitingOrderHandler;

import no.nav.aura.basta.backend.vmware.orchestrator.IncompleteVmOrderHandler;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class SpringStartupHook implements ApplicationListener<ContextRefreshedEvent> {


    private WaitingOrderHandler waitingOrderHandler;
    private IncompleteVmOrderHandler incompleteVmOrderHandler;

    @Inject
    public SpringStartupHook(WaitingOrderHandler waitingOrderHandler, IncompleteVmOrderHandler incompleteVmOrderHandler) {
        this.waitingOrderHandler = waitingOrderHandler;
        this.incompleteVmOrderHandler = incompleteVmOrderHandler;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (!waitingOrderHandler.running) {
            waitingOrderHandler.handle();
            waitingOrderHandler.running = true;
        }

        if (!incompleteVmOrderHandler.running) {
            incompleteVmOrderHandler.handle();
            incompleteVmOrderHandler.running = true;
        }
    }
}
