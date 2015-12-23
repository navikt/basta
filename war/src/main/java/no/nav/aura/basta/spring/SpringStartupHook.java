package no.nav.aura.basta.spring;

import javax.inject.Inject;

import no.nav.aura.basta.backend.WaitingOrderHandler;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class SpringStartupHook implements ApplicationListener<ContextRefreshedEvent> {

    private boolean running;
    private WaitingOrderHandler waitingOrderHandler;

    @Inject
    public SpringStartupHook(WaitingOrderHandler waitingOrderHandler) {
        this.waitingOrderHandler = waitingOrderHandler;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (!running) {
            waitingOrderHandler.handle();
            running = true;
        }
    }
}
