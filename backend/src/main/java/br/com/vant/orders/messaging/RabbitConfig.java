package br.com.vant.orders.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("rabbit")
public class RabbitConfig {

    public static final String EXCHANGE = "orders.exchange";
    public static final String QUEUE = "freight.queue";
    public static final String ROUTING_KEY = "order.created";

    @Bean
    TopicExchange ordersExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue freightQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    Binding freightBinding(Queue freightQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(freightQueue).to(ordersExchange).with(ROUTING_KEY);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
