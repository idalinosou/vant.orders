package br.com.vant.orders.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!rabbit")
public class LocalOrderEventPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LocalOrderEventPublisher.class);

    @Override
    public void orderCreated(OrderCreatedEvent event) {
        log.info("Pedido {} publicado no barramento local (perfil rabbit desativado)", event.code());
    }
}
