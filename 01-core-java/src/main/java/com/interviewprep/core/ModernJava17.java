package com.interviewprep.core;

import java.util.ArrayList;
import java.util.List;

/** Java 17 final features only: records, sealed types and instanceof patterns, no preview. */
public final class ModernJava17 {
    record Order(String id, List<String> items) {
        Order {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("id is required");
            }
            // Records are shallowly immutable; this copy closes a mutable-list alias.
            items = List.copyOf(items);
        }
    }

    sealed interface Result permits Success, Failure {}
    record Success(Order order) implements Result {}
    record Failure(String message) implements Result {}

    static String describe(Result result) {
        if (result instanceof Success success) {
            return "order " + success.order().id();
        }
        if (result instanceof Failure failure) {
            return "failed: " + failure.message();
        }
        // Pattern switch is preview in 17, so this example uses finalized instanceof.
        // Unlike an exhaustive pattern switch, adding a subtype won't invalidate this chain.
        throw new IllegalArgumentException("result cannot be null");
    }

    static String priority(int level) {
        return switch (level) {
            case 1 -> "urgent";
            case 2, 3 -> "normal";
            default -> "low";
        };
    }

    public static void main(String[] args) {
        List<String> input = new ArrayList<>(List.of("book"));
        Order order = new Order("O-1", input);
        input.add("unexpected");
        Checks.equal(List.of("book"), order.items(), "defensive copy isolates record state");
        Checks.equal(new Order("O-1", List.of("book")), order, "record value equality");
        Checks.throwsType(UnsupportedOperationException.class, () -> order.items().clear(),
                "accessor cannot expose mutation");
        Checks.throwsType(IllegalArgumentException.class, () -> new Order(" ", List.of()),
                "compact constructor validates before assignment");
        Checks.equal("order O-1", describe(new Success(order)), "pattern narrows type");
        Checks.equal("failed: unavailable", describe(new Failure("unavailable")), "failure branch");
        Checks.equal("normal", priority(3), "switch expression returns a value");
        System.out.println("ModernJava17 passed: records, sealed hierarchy, non-preview patterns.");
    }
}
