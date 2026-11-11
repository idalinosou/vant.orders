package br.com.vant.orders.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderEventRelay {

    private static final Logger log = LoggerFactory.getLogger(OrderEventRelay.class);

    private final OrderEventPublisher publisher;

    public OrderEventRelay(OrderEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        try {
            publisher.orderCreated(event);
        } catch (RuntimeException error) {
            log.error("Falha ao publicar o pedido {}: {}", event.code(), error.getMessage());
        }
    }
}
