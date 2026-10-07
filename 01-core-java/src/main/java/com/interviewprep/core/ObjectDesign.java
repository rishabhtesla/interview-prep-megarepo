package com.interviewprep.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Value identity and replaceable behavior; inheritance is not the default reuse mechanism. */
public final class ObjectDesign {
    static final class CustomerId {
        private final String value;

        CustomerId(String value) {
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public boolean equals(Object other) {
            // final prevents a subclass from introducing asymmetric equality.
            return this == other || other instanceof CustomerId id && value.equals(id.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    interface Discount {
        long apply(long cents);
    }

    static final class Checkout {
        private final Discount discount;

        Checkout(Discount discount) {
            this.discount = Objects.requireNonNull(discount);
        }

        long total(long cents) {
            if (cents < 0) {
                throw new IllegalArgumentException("negative price");
            }
            long total = discount.apply(cents);
            // A strategy must honor the interface contract, not merely its signature.
            if (total < 0 || total > cents) {
                throw new IllegalStateException("discount violated its contract");
            }
            return total;
        }
    }

    public static void main(String[] args) {
        CustomerId a = new CustomerId("C-1");
        CustomerId b = new CustomerId("C-1");
        CustomerId c = new CustomerId("C-1");
        Checks.that(a.equals(a), "reflexive equality");
        Checks.that(a.equals(b) && b.equals(a), "symmetric equality");
        Checks.that(a.equals(b) && b.equals(c) && a.equals(c), "transitive equality");
        Checks.that(!a.equals(null) && !a.equals("C-1"), "unrelated types aren't equal");
        Checks.equal(a.hashCode(), b.hashCode(), "equal objects require equal hashes");
        Map<CustomerId, String> customers = new HashMap<>();
        customers.put(a, "Ada");
        Checks.equal("Ada", customers.get(b), "lookup uses logical value equality");

        // Inject a behavior instead of subclassing Checkout for every pricing policy.
        Checkout standard = new Checkout(cents -> cents);
        Checkout tenPercent = new Checkout(cents -> cents - cents / 10);
        Checks.equal(1_000L, standard.total(1_000), "identity strategy");
        Checks.equal(900L, tenPercent.total(1_000), "polymorphism through an interface");
        Checks.equal(91L, tenPercent.total(101), "rounding policy is explicit in cents");
        Checks.throwsType(IllegalArgumentException.class, () -> standard.total(-1),
                "validate input at the boundary");
        Checks.throwsType(IllegalStateException.class,
                () -> new Checkout(cents -> cents + 1).total(100), "validate strategy contract");
        System.out.println("ObjectDesign passed: equality laws and composition contracts.");
    }
}
