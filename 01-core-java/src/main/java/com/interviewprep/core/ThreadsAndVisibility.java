package com.interviewprep.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** Deterministic schedules expose why visibility and atomicity are different properties. */
public final class ThreadsAndVisibility {
    static final class VolatileCounter {
        volatile int value;
    }

    static void await(CountDownLatch latch) throws InterruptedException {
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new AssertionError("timed out waiting for worker coordination");
        }
    }

    static void join(Thread worker) throws InterruptedException {
        worker.join(5_000);
        if (worker.isAlive()) {
            worker.interrupt();
            throw new AssertionError("worker did not finish");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        VolatileCounter broken = new VolatileCounter();
        CountDownLatch bothRead = new CountDownLatch(2);
        CountDownLatch allowWrite = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Runnable lostUpdate = () -> {
            try {
                // Deliberately split the same read-modify-write steps as value++.
                // Both threads read zero before either writes one.
                int previous = broken.value;
                bothRead.countDown();
                await(allowWrite);
                broken.value = previous + 1;
            } catch (Throwable problem) {
                failure.compareAndSet(null, problem);
            }
        };
        Thread first = new Thread(lostUpdate, "first");
        Thread second = new Thread(lostUpdate, "second");
        first.start(); // start schedules a new thread; calling run() would run inline.
        second.start();
        try {
            await(bothRead);
        } finally {
            allowWrite.countDown();
        }
        join(first);
        join(second);
        Checks.equal(null, failure.get(), "worker exceptions must reach the test");
        Checks.equal(1, broken.value, "volatile does not make a compound operation atomic");

        AtomicInteger atomic = new AtomicInteger();
        Runnable increment = () -> {
            for (int i = 0; i < 10_000; i++) {
                atomic.incrementAndGet();
            }
        };
        Thread left = new Thread(increment);
        Thread right = new Thread(increment);
        left.start();
        right.start();
        join(left);
        join(right);
        // Successful join gives a happens-before edge for the worker's actions.
        Checks.equal(20_000, atomic.get(), "atomic increment retains all updates");

        AtomicReference<String> executedBy = new AtomicReference<>();
        Thread notStarted = new Thread(() -> executedBy.set(Thread.currentThread().getName()));
        notStarted.run();
        Checks.equal(Thread.currentThread().getName(), executedBy.get(), "run is an ordinary call");
        System.out.println("ThreadsAndVisibility passed: forced lost update versus atomics.");
    }
}
