package com.interviewprep.foundation;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final OrderStore store;

    public OrderService(OrderStore store) {
        this.store = store;
    }

    public List<OrderResponse> list() {
        return store.findAll().stream().map(OrderResponse::from).toList();
    }

    public OrderResponse create(OrderRequest request) {
        // Validate first at the HTTP boundary; then keep the business calculation here.
        int totalCents = Math.multiplyExact(request.quantity(), request.unitPriceCents());
        return OrderResponse.from(store.save(request.item().trim(), request.quantity(), totalCents));
    }

    public OrderResponse get(long id) {
        return store.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
