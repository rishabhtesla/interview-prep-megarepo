package com.interviewprep.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** A pipeline is a query, not a container; Optional models absence, not every error. */
public final class FunctionalPipelines {
    static Optional<Integer> parsePositive(String input) {
        try {
            int value = Integer.parseInt(input);
            return value > 0 ? Optional.of(value) : Optional.empty();
        } catch (NumberFormatException invalid) {
            return Optional.empty();
        }
    }

    public static void main(String[] args) {
        List<String> tokens = List.of("3", "bad", "-1", "4", "3");
        List<Integer> valid = tokens.stream()
                .map(FunctionalPipelines::parsePositive)
                .flatMap(Optional::stream)
                .toList();
        Checks.equal(List.of(3, 4, 3), valid, "flatMap discards absence and unwraps presence");
        int total = valid.stream().mapToInt(Integer::intValue).sum();
        Checks.equal(10, total, "primitive stream avoids boxed reduction state");
        Map<Integer, Long> counts = valid.stream().collect(
                Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Checks.equal(Map.of(3, 2L, 4, 1L), counts, "grouping supports duplicate keys");
        Checks.throwsType(IllegalStateException.class,
                () -> valid.stream().collect(Collectors.toMap(Function.identity(), x -> x)),
                "toMap without a merge function rejects duplicate keys");

        AtomicInteger fallbackCalls = new AtomicInteger();
        Optional<String> present = Optional.of("available");
        present.orElseGet(() -> {
            fallbackCalls.incrementAndGet();
            return "fallback";
        });
        Checks.equal(0, fallbackCalls.get(), "orElseGet is lazy");
        present.orElse("fallback-" + fallbackCalls.incrementAndGet());
        Checks.equal(1, fallbackCalls.get(), "orElse argument is evaluated eagerly");
        Checks.that(parsePositive("0").isEmpty(), "zero is rejected by the domain rule");

        // Effectively-final capture is a restriction on the variable, not object immutability.
        int factor = 2;
        Checks.equal(List.of(6, 8, 6), valid.stream().map(x -> x * factor).toList(),
                "stateless lambda captures an unchanged factor");
        Stream<Integer> once = valid.stream();
        Checks.equal(3L, once.count(), "terminal operation consumes the stream");
        Checks.throwsType(IllegalStateException.class, once::count, "streams cannot be reused");
        Checks.throwsType(UnsupportedOperationException.class, () -> valid.add(5),
                "Stream.toList is unmodifiable in Java 17");
        System.out.println("FunctionalPipelines passed: absence, aggregation, lazy fallback.");
    }
}
