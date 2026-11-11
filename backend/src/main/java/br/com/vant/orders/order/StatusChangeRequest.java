package br.com.vant.orders.order;

import jakarta.validation.constraints.NotNull;

public record StatusChangeRequest(@NotNull(message = "Informe o status desejado") OrderStatus status) {
}
