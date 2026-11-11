package br.com.vant.orders.order;

import br.com.vant.orders.web.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Pedidos", description = "Criacao, consulta e acompanhamento de pedidos de transporte")
public class OrderController {

    private final OrderService service;
    private final String internalToken;

    public OrderController(OrderService service, @Value("${app.internal.token}") String internalToken) {
        this.service = service;
        this.internalToken = internalToken;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um pedido e publica o evento order.created")
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return service.create(request);
    }

    @GetMapping
    @Operation(summary = "Lista pedidos, com filtro opcional por status e termo")
    public List<OrderResponse> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String q) {
        return service.list(status, q);
    }

    @GetMapping("/summary")
    @Operation(summary = "Total de pedidos por status")
    public Map<String, Long> summary() {
        return service.summary();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe de um pedido")
    public OrderResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping("/{id}/quote")
    @Operation(summary = "Registra a cotacao de frete devolvida pelo worker")
    public OrderResponse quote(
            @PathVariable UUID id,
            @Valid @RequestBody QuoteRequest request,
            @RequestHeader(name = "X-Internal-Token", required = false) String token) {
        if (!internalToken.equals(token)) {
            throw new UnauthorizedException("Credencial interna invalida");
        }
        return service.quote(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Avanca o status do pedido (COTADO, DESPACHADO, ENTREGUE)")
    public OrderResponse changeStatus(@PathVariable UUID id, @Valid @RequestBody StatusChangeRequest request) {
        return service.changeStatus(id, request.status());
    }
}
