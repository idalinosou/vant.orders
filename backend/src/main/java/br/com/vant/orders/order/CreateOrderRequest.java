package br.com.vant.orders.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotBlank(message = "Informe o nome do cliente")
        @Size(max = 120, message = "Nome do cliente deve ter ate 120 caracteres")
        String customerName,

        @NotBlank(message = "Informe o CEP de origem")
        @Pattern(regexp = "\\d{8}", message = "CEP de origem deve ter 8 digitos")
        String originCep,

        @NotBlank(message = "Informe o CEP de destino")
        @Pattern(regexp = "\\d{8}", message = "CEP de destino deve ter 8 digitos")
        String destinationCep,

        @NotNull(message = "Informe o peso da carga")
        @DecimalMin(value = "0.01", message = "Peso deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "Peso invalido")
        BigDecimal weightKg) {
}
