package com.interviewprep.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Task submission, observed failures, cancellation and explicit Java 17 executor ownership. */
public final class ExecutorsAndFutures {
    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch release = new CountDownLatch(1);
        try {
            Future<Integer> answer = pool.submit(() -> 6 * 7);
            Checks.equal(42, answer.get(5, TimeUnit.SECONDS), "Callable returns a result");

            Future<Integer> failed = pool.submit(() -> {
                throw new IllegalArgumentException("bad task");
            });
            try {
                failed.get(5, TimeUnit.SECONDS);
                throw new AssertionError("task failure disappeared");
            } catch (ExecutionException wrapped) {
                Checks.that(wrapped.getCause() instanceof IllegalArgumentException,
                        "get exposes task failure through ExecutionException");
            }

            Future<String> waiting = pool.submit(() -> {
                release.await(); // Interruptible blocking permits cooperative cancellation.
                return "released";
            });
            try {
                waiting.get(20, TimeUnit.MILLISECONDS);
                throw new AssertionError("unreleased task unexpectedly finished");
            } catch (TimeoutException expected) {
                Checks.that(!waiting.isDone(), "timeout does not cancel the task");
            }
            Checks.that(waiting.cancel(true), "request interruption or cancel before start");
            Checks.that(waiting.isCancelled(), "future records cancellation");

            CompletableFuture<Integer> composed = CompletableFuture
                    .supplyAsync(() -> 20, pool)
                    .thenApply(value -> value + 1)
                    .thenCompose(value -> CompletableFuture.supplyAsync(() -> value * 2, pool));
            Checks.equal(42, composed.get(5, TimeUnit.SECONDS), "compose flattens nested stages");
            int recovered = CompletableFuture.<Integer>failedFuture(
                    new IllegalStateException("unavailable")).exceptionally(error -> -1).join();
            Checks.equal(-1, recovered, "recovery converts a failure into a value");
        } finally {
            release.countDown();
            // Java 17 ExecutorService is not AutoCloseable. shutdown rejects new work;
            // awaiting completion and shutdownNow fallback make ownership explicit.
            pool.shutdown();
            try {
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                    Checks.that(pool.awaitTermination(5, TimeUnit.SECONDS), "workers terminated");
                }
            } catch (InterruptedException interrupted) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
                throw interrupted;
            }
        }
        System.out.println("ExecutorsAndFutures passed: results, failure, timeout, composition.");
    }
}
