package br.com.vant.orders.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        String code,
        String originCep,
        String destinationCep,
        BigDecimal weightKg) {
}
