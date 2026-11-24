package com.interviewprep.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * A first executable pass through the Java language.
 *
 * <p>Run this class before the specialised lessons. Every section is deliberately small:
 * read the comments, predict the checks, run the program, and then change one line.
 * The later lessons explain the same ideas in more depth and include failure cases.
 */
public final class ZeroToHero {
    private ZeroToHero() {}

    public static void main(String[] args) throws Exception {
        basics();
        arraysAndStrings();
        objectDesign();
        collectionsAndGenerics();
        exceptionsAndValidation();
        streamsAndOptional();
        concurrency();
        interviewPattern();
        System.out.println("Zero-to-hero Java tour passed.");
    }

    private static void basics() {
        System.out.println("\n1) Values, expressions, conditions, and methods");

        // A primitive variable stores a value directly. A final variable cannot be reassigned.
        int priceCents = 1_299;
        final int quantity = 2;
        int subtotal = priceCents * quantity;

        // An if/else chooses one branch; a loop repeats while its condition is true.
        String priceBand;
        if (subtotal >= 2_000) {
            priceBand = "large";
        } else {
            priceBand = "small";
        }

        int digitSum = 0;
        for (int value = subtotal; value > 0; value /= 10) {
            digitSum += value % 10;
        }

        // A method receives values and returns a value; Java does not have output parameters.
        int total = addTax(subtotal, 18);
        Checks.equal(3_065, total, "tax calculation");
        Checks.equal("large", priceBand, "branch result");
        Checks.equal(24, digitSum, "loop result");
        System.out.println("subtotal=" + subtotal + ", total=" + total);
    }

    private static int addTax(int amountCents, int taxPercent) {
        return amountCents + amountCents * taxPercent / 100;
    }

    private static void arraysAndStrings() {
        System.out.println("\n2) Arrays and strings");

        // Arrays have a fixed length and use zero-based indexes.
        int[] scores = {70, 85, 90};
        scores[0] += 5;
        Checks.equal(75, scores[0], "array mutation");
        Checks.equal(3, scores.length, "array length");

        // String is immutable: trim() returns a new String rather than changing the old one.
        String rawName = "  Ada Lovelace  ";
        String name = rawName.trim();
        Checks.equal("  Ada Lovelace  ", rawName, "original string remains unchanged");
        Checks.equal("Ada Lovelace", name, "trimmed string");

        // StringBuilder is the mutable choice for repeated concatenation.
        StringBuilder initials = new StringBuilder();
        for (String part : name.split(" ")) {
            initials.append(part.charAt(0));
        }
        Checks.equal("AL", initials.toString(), "built initials");
    }

    private static void objectDesign() {
        System.out.println("\n3) Classes, records, interfaces, and composition");

        // A record is a compact immutable value object with value-based equals/hashCode.
        Product book = new Product("Java interview guide", 2_500);
        PriceRule memberRule = new PercentageDiscount(10);
        int finalPrice = memberRule.apply(book.priceCents());

        Checks.equal("Java interview guide", book.name(), "record accessor");
        Checks.equal(2_250, finalPrice, "composed pricing rule");
        Checks.equal(book, new Product("Java interview guide", 2_500), "record equality");
    }

    private record Product(String name, int priceCents) {
        private Product {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name is required");
            }
            if (priceCents < 0) {
                throw new IllegalArgumentException("price cannot be negative");
            }
        }
    }

    private interface PriceRule {
        int apply(int priceCents);
    }

    private record PercentageDiscount(int percent) implements PriceRule {
        private PercentageDiscount {
            if (percent < 0 || percent > 100) {
                throw new IllegalArgumentException("percent must be between 0 and 100");
            }
        }

        @Override
        public int apply(int priceCents) {
            return priceCents - priceCents * percent / 100;
        }
    }

    private static void collectionsAndGenerics() {
        System.out.println("\n4) Collections, maps, sets, queues, and generics");

        // List keeps order, Set removes duplicates, and Map associates a key with a value.
        List<String> topics = new ArrayList<>(List.of("Java", "Spring", "Java"));
        Set<String> uniqueTopics = new HashSet<>(topics);
        Map<String, Integer> minutesByTopic = new HashMap<>();
        for (String topic : topics) {
            minutesByTopic.merge(topic, 30, Integer::sum);
        }

        // Deque is a queue at one end and a stack at the other; ArrayDeque is usually preferred.
        ArrayDeque<String> reviewQueue = new ArrayDeque<>();
        reviewQueue.addLast("Java");
        reviewQueue.addLast("Spring");
        String next = reviewQueue.removeFirst();

        Checks.equal(List.of("Java", "Spring"), uniqueTopics.stream().sorted().toList(),
                "set removes duplicate");
        Checks.equal(60, minutesByTopic.get("Java"), "map merge");
        Checks.equal("Java", next, "FIFO queue");

        // PECS: a producer extends Number, while a consumer can accept Integer values.
        List<Integer> numbers = new ArrayList<>();
        addDefaults(numbers);
        Checks.equal(6.0, sumNumbers(numbers), "generic helper methods");
    }

    private static void addDefaults(List<? super Integer> destination) {
        destination.add(1);
        destination.add(2);
        destination.add(3);
    }

    private static double sumNumbers(List<? extends Number> numbers) {
        double sum = 0;
        for (Number number : numbers) {
            sum += number.doubleValue();
        }
        return sum;
    }

    private static void exceptionsAndValidation() {
        System.out.println("\n5) Exceptions and explicit failure contracts");

        // Exceptions carry failure information; callers choose whether to recover or propagate it.
        int validScore = parseScore("87");
        Checks.equal(87, validScore, "valid score");
        Checks.throwsType(InvalidScoreException.class, () -> parseScore("not-a-score"),
                "malformed score must fail clearly");

        try {
            validateScore(120);
            throw new AssertionError("out-of-range score should fail");
        } catch (InvalidScoreException expected) {
            Checks.that(expected.getMessage().contains("0..100"),
                    "exception should explain the contract");
        }
    }

    private static int parseScore(String text) {
        try {
            int score = Integer.parseInt(text);
            validateScore(score);
            return score;
        } catch (NumberFormatException failure) {
            throw new InvalidScoreException("score must be an integer", failure);
        }
    }

    private static void validateScore(int score) {
        if (score < 0 || score > 100) {
            throw new InvalidScoreException("score must be in the range 0..100");
        }
    }

    private static final class InvalidScoreException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private InvalidScoreException(String message) {
            super(message);
        }

        private InvalidScoreException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static void streamsAndOptional() {
        System.out.println("\n6) Lambdas, streams, and Optional");

        List<String> names = List.of("Ada", "Grace", "Alan", "Linus");
        List<String> longNames = names.stream()
                .filter(name -> name.length() >= 4)
                .map(String::toUpperCase)
                .sorted(Comparator.naturalOrder())
                .toList();

        // Optional makes the absence case visible at the call site instead of returning null.
        Optional<String> found = names.stream()
                .filter(name -> name.startsWith("G"))
                .findFirst();
        String greeting = found.map(name -> "Hello, " + name).orElse("Hello, guest");

        // A stream is a one-use pipeline: collect it once, then create a new stream if needed.
        Map<Integer, List<String>> namesByLength = names.stream()
                .collect(Collectors.groupingBy(String::length));

        Checks.equal(List.of("ALAN", "GRACE", "LINUS"), longNames, "stream pipeline");
        Checks.equal("Hello, Grace", greeting, "optional present branch");
        Checks.equal(List.of("Ada"), namesByLength.get(3), "grouping collector");
    }

    private static void concurrency() throws InterruptedException, ExecutionException {
        System.out.println("\n7) Tasks, futures, and atomic state");

        // AtomicInteger combines read-modify-write into one atomic operation.
        AtomicInteger completedTasks = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> first = executor.submit(() -> {
                completedTasks.incrementAndGet();
                return 20;
            });
            Future<Integer> second = executor.submit(() -> {
                completedTasks.incrementAndGet();
                return 22;
            });

            Checks.equal(42, first.get() + second.get(), "future results");
            Checks.equal(2, completedTasks.get(), "atomic counter");
        } finally {
            // The owner that creates an executor must shut it down.
            executor.shutdown();
        }
    }

    private static void interviewPattern() {
        System.out.println("\n8) A small interview pattern: two sum");

        int[] input = {2, 7, 11, 15};
        int[] answer = twoSum(input, 9);
        Checks.equal(0, answer[0], "two-sum first index");
        Checks.equal(1, answer[1], "two-sum second index");
        System.out.println("indices=" + answer[0] + "," + answer[1]);
    }

    private static int[] twoSum(int[] values, int target) {
        Map<Integer, Integer> indexByValue = new HashMap<>();
        for (int index = 0; index < values.length; index++) {
            int complement = target - values[index];
            Integer previousIndex = indexByValue.get(complement);
            if (previousIndex != null) {
                return new int[] {previousIndex, index};
            }
            indexByValue.put(values[index], index);
        }
        throw new IllegalArgumentException("no pair sums to target");
    }
}
