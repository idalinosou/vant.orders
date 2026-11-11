package br.com.vant.orders.order;

import br.com.vant.orders.messaging.OrderCreatedEvent;
import br.com.vant.orders.web.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyMMdd");
    private static final char[] SUFFIX_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final OrderRepository repository;
    private final ApplicationEventPublisher events;
    private final SecureRandom random = new SecureRandom();

    public OrderService(OrderRepository repository, ApplicationEventPublisher events) {
        this.repository = repository;
        this.events = events;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Instant now = Instant.now();
        Order order = Order.create(
                nextCode(),
                request.customerName().trim(),
                request.originCep(),
                request.destinationCep(),
                request.weightKg(),
                now);
        repository.save(order);

        events.publishEvent(new OrderCreatedEvent(
                order.getId(),
                order.getCode(),
                order.getOriginCep(),
                order.getDestinationCep(),
                order.getWeightKg()));

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list(OrderStatus status, String term) {
        String normalized = term == null || term.isBlank() ? null : term.trim();
        return repository.search(status, normalized).stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID id) {
        return OrderResponse.from(find(id));
    }

    @Transactional
    public OrderResponse quote(UUID id, QuoteRequest request) {
        Order order = find(id);
        order.applyQuote(request.freightCents(), request.carrier().trim(), request.estimatedDays(), Instant.now());
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse changeStatus(UUID id, OrderStatus target) {
        Order order = find(id);
        order.moveTo(target, Instant.now());
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> summary() {
        Map<String, Long> summary = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            summary.put(status.name(), 0L);
        }
        for (Object[] row : repository.countByStatus()) {
            summary.put(((OrderStatus) row[0]).name(), ((Number) row[1]).longValue());
        }
        return summary;
    }

    private Order find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido " + id + " nao encontrado"));
    }

    private String nextCode() {
        StringBuilder suffix = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            suffix.append(SUFFIX_ALPHABET[random.nextInt(SUFFIX_ALPHABET.length)]);
        }
        return "ORD-" + CODE_DATE.format(LocalDate.now()) + "-" + suffix;
    }
}
