package com.interviewprep.foundation;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

/**
 * The first repository boundary.
 *
 * <p>This lesson uses memory so the HTTP and dependency-injection flow stays visible. The
 * intermediate Spring module replaces this boundary with JPA/H2 and a transaction.
 */
@Repository
public class OrderStore {
    private final AtomicLong ids = new AtomicLong();
    private final ConcurrentMap<Long, Order> orders = new ConcurrentHashMap<>();

    public Order save(String item, int quantity, int totalCents) {
        long id = ids.incrementAndGet();
        Order order = new Order(id, item, quantity, totalCents, "CREATED");
        orders.put(id, order);
        return order;
    }

    public List<Order> findAll() {
        return orders.values().stream().sorted((left, right) ->
                        Long.compare(left.id(), right.id()))
                .toList();
    }

    public Optional<Order> findById(long id) {
        return Optional.ofNullable(orders.get(id));
    }
}
