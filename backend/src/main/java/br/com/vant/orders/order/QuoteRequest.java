package br.com.vant.orders.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuoteRequest(
        @NotNull(message = "Informe o valor do frete")
        @Min(value = 1, message = "Frete deve ser positivo")
        Integer freightCents,

        @NotBlank(message = "Informe a transportadora")
        @Size(max = 80, message = "Transportadora deve ter ate 80 caracteres")
        String carrier,

        @NotNull(message = "Informe o prazo estimado")
        @Min(value = 1, message = "Prazo minimo de 1 dia")
        @Max(value = 90, message = "Prazo maximo de 90 dias")
        Integer estimatedDays) {
}
