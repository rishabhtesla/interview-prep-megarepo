package com.interviewprep.foundation;

public record OrderResponse(
        long id, String item, int quantity, int totalCents, String status) {
    static OrderResponse from(Order order) {
        return new OrderResponse(
                order.id(),
                order.item(),
                order.quantity(),
                order.totalCents(),
                order.status());
    }
}
