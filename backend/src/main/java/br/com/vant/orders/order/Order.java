package br.com.vant.orders.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders", uniqueConstraints = @UniqueConstraint(name = "uk_orders_code", columnNames = "code"))
public class Order {

    @Id
    private UUID id;

    @Column(nullable = false, length = 24)
    private String code;

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Column(name = "origin_cep", nullable = false, length = 8)
    private String originCep;

    @Column(name = "destination_cep", nullable = false, length = 8)
    private String destinationCep;

    @Column(name = "weight_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal weightKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrderStatus status;

    @Column(name = "freight_cents")
    private Integer freightCents;

    @Column(length = 80)
    private String carrier;

    @Column(name = "estimated_days")
    private Integer estimatedDays;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Order() {
    }

    public static Order create(String code, String customerName, String originCep, String destinationCep,
                               BigDecimal weightKg, Instant now) {
        Order order = new Order();
        order.id = UUID.randomUUID();
        order.code = code;
        order.customerName = customerName;
        order.originCep = originCep;
        order.destinationCep = destinationCep;
        order.weightKg = weightKg;
        order.status = OrderStatus.CREATED;
        order.createdAt = now;
        order.updatedAt = now;
        return order;
    }

    public void applyQuote(int freightCents, String carrier, int estimatedDays, Instant now) {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Cotacao aceita apenas para pedidos recem-criados");
        }
        this.freightCents = freightCents;
        this.carrier = carrier;
        this.estimatedDays = estimatedDays;
        this.status = OrderStatus.QUOTED;
        this.updatedAt = now;
    }

    public void moveTo(OrderStatus target, Instant now) {
        if (!status.canMoveTo(target)) {
            throw new IllegalStateException("Transicao de " + status + " para " + target + " nao permitida");
        }
        this.status = target;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getOriginCep() {
        return originCep;
    }

    public String getDestinationCep() {
        return destinationCep;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Integer getFreightCents() {
        return freightCents;
    }

    public String getCarrier() {
        return carrier;
    }

    public Integer getEstimatedDays() {
        return estimatedDays;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
