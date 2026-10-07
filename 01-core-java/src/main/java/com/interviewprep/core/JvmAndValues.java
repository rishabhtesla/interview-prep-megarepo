package com.interviewprep.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Run: java -cp out com.interviewprep.core.JvmAndValues
 * Observe class loading with -Xlog:class+load=info; GC with -Xlog:gc.
 * This lesson checks language guarantees, not the timing of garbage collection.
 */
public final class JvmAndValues {
    private static int initializations;

    static final class LazyHolder {
        static final int VALUE = initialize();

        private static int initialize() {
            initializations++;
            return 42;
        }
    }

    static void change(int number, List<String> names) {
        number = 99; // A copied primitive cannot update the caller's variable.
        names.add("Grace"); // A copied reference still points at the same object.
        names = new ArrayList<>(); // Reassigning that copy does not rebind the caller.
        names.add("local only");
    }

    public static void main(String[] args) throws ClassNotFoundException {
        int number = 7;
        List<String> names = new ArrayList<>(List.of("Ada"));
        change(number, names);
        Checks.equal(7, number, "Java passes the primitive value");
        Checks.equal(List.of("Ada", "Grace"), names, "Java also passes reference values");

        // Loading a class and initializing its static state are separate operations.
        // Use the binary nested-class name without touching LazyHolder.VALUE.
        Class<?> loaded = Class.forName(
                JvmAndValues.class.getName() + "$LazyHolder", false,
                JvmAndValues.class.getClassLoader());
        int before = initializations;
        Checks.equal(42, LazyHolder.VALUE, "first active use initializes the class");
        Checks.equal(42, LazyHolder.VALUE, "subsequent use reuses its static state");
        Checks.that(initializations == 1 && before <= 1, "initialization happens once per loader");
        Checks.equal(LazyHolder.class, loaded, "same loader plus binary name means same class");
        Checks.equal(null, String.class.getClassLoader(), "bootstrap loader is exposed as null");

        // Reachability, not scope alone, determines GC eligibility. A live collection
        // retains elements; clearing it removes these edges, not necessarily all roots.
        List<byte[]> retained = new ArrayList<>();
        retained.add(new byte[1_024]);
        Checks.equal(1, retained.size(), "container retains the array");
        retained.clear();
        Checks.that(retained.isEmpty(), "container no longer retains the array");
        System.out.println("JvmAndValues passed: copied values, class identity, reachability.");
    }
}
