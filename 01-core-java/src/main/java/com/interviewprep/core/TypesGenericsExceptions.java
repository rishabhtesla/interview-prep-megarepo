package com.interviewprep.core;

import java.util.ArrayList;
import java.util.List;

/** Primitive arithmetic, PECS, erasure, and exception ownership in one boundary example. */
public final class TypesGenericsExceptions {
    static double sum(List<? extends Number> source) {
        // Producer extends: reading Number is safe; inserting any specific subtype isn't.
        double total = 0;
        for (Number value : source) {
            total += value.doubleValue();
        }
        return total;
    }

    static void copyIntegers(List<? extends Integer> source, List<? super Integer> target) {
        // Consumer super: Integer is always safe to insert; reads promise only Object.
        target.addAll(source);
    }

    static final class ClosingResource implements AutoCloseable {
        boolean closed;

        @Override
        public void close() {
            closed = true;
            throw new IllegalStateException("close failed");
        }
    }

    public static void main(String[] args) {
        Checks.equal(2, 5 / 2, "integer division truncates");
        Checks.equal(2.5, 5 / 2.0, "promotion happens before division");
        Checks.equal(Integer.MIN_VALUE, Integer.MAX_VALUE + 1, "ordinary overflow wraps");
        Checks.throwsType(ArithmeticException.class,
                () -> Math.addExact(Integer.MAX_VALUE, 1), "exact arithmetic detects overflow");
        Integer absent = null;
        Checks.throwsType(NullPointerException.class, () -> {
            int unboxed = absent;
            System.out.println(unboxed);
        }, "unboxing null fails");

        List<Integer> source = List.of(2, 3);
        List<Number> destination = new ArrayList<>();
        copyIntegers(source, destination);
        Checks.equal(5.0, sum(destination), "bounded wildcards preserve type safety");
        Checks.equal(new ArrayList<String>().getClass(), new ArrayList<Integer>().getClass(),
                "generic arguments are erased, not distinct runtime classes");

        // Arrays are covariant and enforce their element type at runtime. Lists instead
        // reject List<String> -> List<Object> at compile time (generic invariance).
        Object[] covariant = new String[1];
        Checks.throwsType(ArrayStoreException.class, () -> covariant[0] = 1,
                "array covariance postpones this error to runtime");

        ClosingResource resource = new ClosingResource();
        try (resource) {
            throw new IllegalArgumentException("body failed");
        } catch (IllegalArgumentException primary) {
            Checks.equal("body failed", primary.getMessage(), "body failure stays primary");
            Checks.equal(1, primary.getSuppressed().length, "cleanup failure is retained");
            Checks.equal("close failed", primary.getSuppressed()[0].getMessage(),
                    "suppression preserves diagnostic evidence");
        }
        Checks.that(resource.closed, "cleanup ran on the exceptional path");
        System.out.println("TypesGenericsExceptions passed: arithmetic, PECS, suppression.");
    }
}
