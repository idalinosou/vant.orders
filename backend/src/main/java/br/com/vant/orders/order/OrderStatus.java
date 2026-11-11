package br.com.vant.orders.order;

public enum OrderStatus {
    CREATED,
    QUOTED,
    DISPATCHED,
    DELIVERED;

    public boolean canMoveTo(OrderStatus target) {
        return switch (this) {
            case CREATED -> target == QUOTED;
            case QUOTED -> target == DISPATCHED;
            case DISPATCHED -> target == DELIVERED;
            case DELIVERED -> false;
        };
    }
}
