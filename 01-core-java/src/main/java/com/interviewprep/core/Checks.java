package com.interviewprep.core;

import java.util.Objects;

/** Explicit checks run even without -ea; no test framework dependency is needed. */
final class Checks {
    private Checks() {}

    static void equal(Object expected, Object actual, String reason) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(reason + ": expected " + expected + ", got " + actual);
        }
    }

    static void that(boolean condition, String reason) {
        if (!condition) {
            throw new AssertionError(reason);
        }
    }

    static void throwsType(Class<? extends Throwable> type, Runnable action, String reason) {
        try {
            action.run();
        } catch (Throwable failure) {
            if (type.isInstance(failure)) {
                return;
            }
            throw new AssertionError(reason + ": wrong exception", failure);
        }
        throw new AssertionError(reason + ": no exception");
    }
}
