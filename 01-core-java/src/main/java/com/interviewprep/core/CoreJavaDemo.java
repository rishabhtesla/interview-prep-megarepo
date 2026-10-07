package com.interviewprep.core;

/** Run the whole course; individual lessons also have their own main method. */
public final class CoreJavaDemo {
    private CoreJavaDemo() {}

    public static void main(String[] args) throws Exception {
        JvmAndValues.main(args);
        TypesGenericsExceptions.main(args);
        ObjectDesign.main(args);
        CollectionChoices.main(args);
        ThreadsAndVisibility.main(args);
        ExecutorsAndFutures.main(args);
        LocksAndConcurrentMaps.main(args);
        FunctionalPipelines.main(args);
        ModernJava17.main(args);
        TestingAndPerformance.main(args);
        System.out.println("All 10 Java lessons passed.");
    }
}
