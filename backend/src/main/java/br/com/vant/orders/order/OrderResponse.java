package br.com.vant.orders.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String code,
        String customerName,
        String originCep,
        String destinationCep,
        BigDecimal weightKg,
        OrderStatus status,
        Integer freightCents,
        String carrier,
        Integer estimatedDays,
        Instant createdAt,
        Instant updatedAt) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCode(),
                order.getCustomerName(),
                order.getOriginCep(),
                order.getDestinationCep(),
                order.getWeightKg(),
                order.getStatus(),
                order.getFreightCents(),
                order.getCarrier(),
                order.getEstimatedDays(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
