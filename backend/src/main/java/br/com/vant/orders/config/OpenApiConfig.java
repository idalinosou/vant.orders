package br.com.vant.orders.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI ordersOpenApi() {
        return new OpenAPI().info(new Info()
                .title("vant.orders API")
                .version("1.0.0")
                .description("""
                        API de pedidos de transporte com cotacao de frete assincrona.

                        O fluxo principal: o pedido nasce em CREATED, e publicado na exchange
                        orders.exchange, o worker de frete consome a mensagem e devolve a
                        cotacao em POST /api/orders/{id}/quote, autenticando com o header
                        X-Internal-Token.
                        """)
                .contact(new Contact().name("Victor Idalino")));
    }
}
