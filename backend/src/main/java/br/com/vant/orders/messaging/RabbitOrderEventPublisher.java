package br.com.vant.orders.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("rabbit")
public class RabbitOrderEventPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitOrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitOrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void orderCreated(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                event);
        log.info("Pedido {} publicado na exchange {}", event.code(), RabbitConfig.EXCHANGE);
    }
}
