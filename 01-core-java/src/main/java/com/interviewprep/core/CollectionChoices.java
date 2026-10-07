package com.interviewprep.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** Choose operations and contracts before choosing a concrete collection. */
public final class CollectionChoices {
    static Map<String, Integer> frequencies(List<String> words) {
        Map<String, Integer> result = new HashMap<>();
        for (String word : words) {
            // One pass: expected O(n), assuming reasonable hash distribution.
            result.merge(word, 1, Integer::sum);
        }
        return result;
    }

    public static void main(String[] args) {
        Checks.equal(Map.of("java", 2, "scala", 1),
                frequencies(List.of("java", "scala", "java")), "count repeated keys");
        Checks.that(frequencies(List.of()).isEmpty(), "empty input is valid");

        List<String> original = new ArrayList<>(List.of("a", "b", "c"));
        List<String> view = original.subList(0, 2);
        List<String> snapshot = List.copyOf(view);
        view.set(0, "changed");
        Checks.equal("changed", original.get(0), "subList is backed by the parent");
        Checks.equal("a", snapshot.get(0), "copyOf snapshots the elements");
        Checks.throwsType(UnsupportedOperationException.class, () -> snapshot.add("x"),
                "unmodifiable API rejects structural updates");
        // The copy is shallow: nested mutable elements would still be shared.

        TreeSet<String> byLength = new TreeSet<>(Comparator.comparingInt(String::length));
        byLength.add("Ada");
        Checks.that(!byLength.add("Bob"), "sorted-set uniqueness uses comparator equality");
        TreeSet<String> totalOrder = new TreeSet<>(
                Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
        totalOrder.addAll(List.of("Ada", "Bob"));
        Checks.equal(2, totalOrder.size(), "tie-breaker preserves distinct names");

        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.addLast(1);
        queue.addLast(2);
        Checks.equal(1, queue.removeFirst(), "FIFO queue");
        queue.addFirst(3);
        Checks.equal(3, queue.removeFirst(), "same deque can model a stack");
        Checks.equal(2, queue.removeFirst(), "remaining queue element");
        Checks.equal(null, queue.pollFirst(), "poll handles an empty deque");
        Checks.throwsType(NullPointerException.class, () -> queue.addLast(null),
                "null would be ambiguous with poll's empty marker");
        System.out.println("CollectionChoices passed: counting, views, ordering, deque.");
    }
}
