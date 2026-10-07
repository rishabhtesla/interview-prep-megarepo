package com.interviewprep.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/** Protect invariants, not just individual fields; avoid deadlock by a total lock order. */
public final class LocksAndConcurrentMaps {
    static final class Account {
        final int id;
        final ReentrantLock lock = new ReentrantLock();
        long cents;

        Account(int id, long cents) {
            this.id = id;
            this.cents = cents;
        }
    }

    static void transfer(Account from, Account to, long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("negative transfer");
        }
        if (from == to) {
            return;
        }
        if (from.id == to.id) {
            throw new IllegalArgumentException("distinct accounts need unique ordering IDs");
        }
        Account first = from.id < to.id ? from : to;
        Account second = from.id < to.id ? to : from;
        first.lock.lock();
        try {
            second.lock.lock();
            try {
                if (from.cents < amount) {
                    throw new IllegalArgumentException("insufficient funds");
                }
                // Compute before mutating, so overflow leaves both accounts unchanged.
                long newDestination = Math.addExact(to.cents, amount);
                from.cents -= amount;
                to.cents = newDestination;
            } finally {
                second.lock.unlock();
            }
        } finally {
            first.lock.unlock();
        }
    }

    static final class Inventory {
        private int stock = 1;

        synchronized boolean reserve() {
            // Check plus update is one critical section on the same monitor.
            if (stock == 0) {
                return false;
            }
            stock--;
            return true;
        }
    }

    public static void main(String[] args) throws Exception {
        Account a = new Account(1, 10_000);
        Account b = new Account(2, 10_000);
        ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();
        Inventory inventory = new Inventory();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int worker = 0; worker < 4; worker++) {
                boolean forward = worker % 2 == 0;
                results.add(pool.submit(() -> {
                    for (int i = 0; i < 1_000; i++) {
                        transfer(forward ? a : b, forward ? b : a, 1);
                        // Atomic for this key; get() followed by put() would lose increments.
                        counts.merge("transfers", 1, Integer::sum);
                    }
                    return inventory.reserve();
                }));
            }
            int reservations = 0;
            for (Future<Boolean> result : results) {
                if (result.get(10, TimeUnit.SECONDS)) {
                    reservations++;
                }
            }
            Checks.equal(1, reservations, "synchronized check-and-decrement prevents overselling");
            Checks.equal(4_000, counts.get("transfers"), "per-key atomic map updates");
            // No writers remain after get completes; reads are now safely published.
            Checks.equal(10_000L, a.cents, "balanced transfers preserve first balance");
            Checks.equal(20_000L, a.cents + b.cents, "cross-object invariant conserved");
            Checks.throwsType(IllegalArgumentException.class, () -> transfer(a, b, 99_999),
                    "rejected transfer must not mutate");
            Checks.equal(10_000L, a.cents, "failed transfer is unchanged");
            Account full = new Account(3, Long.MAX_VALUE);
            Checks.throwsType(ArithmeticException.class, () -> transfer(a, full, 1),
                    "overflow must not debit the source");
            Checks.equal(10_000L, a.cents, "overflow leaves source unchanged");
        } finally {
            pool.shutdownNow();
            Checks.that(pool.awaitTermination(5, TimeUnit.SECONDS), "executor terminated");
        }
        System.out.println("LocksAndConcurrentMaps passed: invariant, lock order, atomic merge.");
    }
}
