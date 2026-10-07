package com.interviewprep.core;

import java.util.HashSet;
import java.util.Set;

/** Differential testing compares an optimized implementation with a simple reference. */
public final class TestingAndPerformance {
    static boolean hasPairSlow(int[] values, int target) {
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                // Widen BEFORE arithmetic so overflow cannot manufacture a match.
                if ((long) values[i] + values[j] == target) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean hasPairFast(int[] values, int target) {
        Set<Long> seen = new HashSet<>();
        for (int value : values) {
            long required = (long) target - value;
            if (seen.contains(required)) {
                return true;
            }
            seen.add((long) value); // Check before insert: don't reuse the current element.
        }
        return false;
    }

    public static void main(String[] args) {
        Checks.that(!hasPairFast(new int[]{3}, 6), "one index cannot be used twice");
        Checks.that(hasPairFast(new int[]{3, 3}, 6), "distinct equal elements are valid");
        Checks.that(!hasPairFast(new int[]{Integer.MAX_VALUE, 1}, Integer.MIN_VALUE),
                "arithmetic overflow is not a mathematical match");
        Checks.that(!hasPairFast(new int[]{}, 0), "empty input");
        Checks.that(hasPairFast(new int[]{-3, 7}, 4), "negative values");

        // Exhaust all arrays of length 0..5 over {-2,-1,0,1,2}, then all targets -5..5.
        // Deterministic enumeration finds duplicate/index/boundary bugs without flaky seeds.
        int cases = 0;
        for (int length = 0; length <= 5; length++) {
            int combinations = 1;
            for (int i = 0; i < length; i++) {
                combinations *= 5;
            }
            for (int encoded = 0; encoded < combinations; encoded++) {
                int[] values = new int[length];
                int remaining = encoded;
                for (int i = 0; i < length; i++) {
                    values[i] = remaining % 5 - 2;
                    remaining /= 5;
                }
                for (int target = -5; target <= 5; target++) {
                    Checks.equal(hasPairSlow(values, target), hasPairFast(values, target),
                            "optimized implementation agrees with independent oracle");
                    cases++;
                }
            }
        }
        System.out.println("TestingAndPerformance passed: " + cases + " differential cases.");
        // No nanoTime contest: JIT warm-up, dead-code elimination and allocation noise
        // would make a single ad-hoc timing an unreliable performance claim.
    }
}
