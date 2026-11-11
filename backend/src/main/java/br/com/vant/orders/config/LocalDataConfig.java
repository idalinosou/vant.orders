package br.com.vant.orders.config;

import br.com.vant.orders.order.Order;
import br.com.vant.orders.order.OrderRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Configuration
@Profile("local")
public class LocalDataConfig {

    @Bean
    ApplicationRunner sampleOrders(OrderRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            Instant now = Instant.now();

            Order quoted = Order.create("ORD-260101-8K2A", "Transportes Vale Verde", "30130010", "20040020",
                    new BigDecimal("480.00"), now.minus(Duration.ofHours(6)));
            quoted.applyQuote(129900, "Rodo Sul Cargas", 3, now.minus(Duration.ofHours(5)));

            Order dispatched = Order.create("ORD-260102-M4TR", "Distribuidora Serra Azul", "31270010", "29090500",
                    new BigDecimal("1250.50"), now.minus(Duration.ofHours(30)));
            dispatched.applyQuote(248000, "Frota Litoral", 4, now.minus(Duration.ofHours(29)));
            dispatched.moveTo(br.com.vant.orders.order.OrderStatus.DISPATCHED, now.minus(Duration.ofHours(20)));

            Order delivered = Order.create("ORD-260103-Q7BZ", "Comercial Pampa", "90010150", "80010010",
                    new BigDecimal("92.30"), now.minus(Duration.ofDays(4)));
            delivered.applyQuote(48900, "Expresso Sul", 2, now.minus(Duration.ofDays(4)).plus(Duration.ofHours(1)));
            delivered.moveTo(br.com.vant.orders.order.OrderStatus.DISPATCHED, now.minus(Duration.ofDays(4)).plus(Duration.ofHours(6)));
            delivered.moveTo(br.com.vant.orders.order.OrderStatus.DELIVERED, now.minus(Duration.ofDays(2)));

            Order pending = Order.create("ORD-260104-C9XD", "Cerealista Boa Safra", "78110000", "38400000",
                    new BigDecimal("310.75"), now.minus(Duration.ofMinutes(40)));

            repository.saveAll(List.of(quoted, dispatched, delivered, pending));
        };
    }
}
