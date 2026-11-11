package br.com.vant.orders.messaging;

public interface OrderEventPublisher {

    void orderCreated(OrderCreatedEvent event);
}
