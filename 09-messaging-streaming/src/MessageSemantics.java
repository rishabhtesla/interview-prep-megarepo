import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Local lesson: at-least-once delivery, bounded retry, dead letters, and
 * idempotent handling. No broker dependency is hidden behind this example.
 */
public final class MessageSemantics {
    private MessageSemantics() {}

    record Event(String id, String type, String payload) {}

    static final class Consumer {
        private final Set<String> processed = new HashSet<>();
        private final Queue<Event> deadLetters = new ArrayDeque<>();
        private int appliedEffects;

        void handle(Event event, int attempt) {
            if (processed.contains(event.id())) {
                System.out.println("duplicate ignored: " + event.id());
                return;
            }
            if ("transient".equals(event.payload()) && attempt < 3) {
                throw new IllegalStateException("temporary downstream failure");
            }
            if ("poison".equals(event.payload())) {
                if (attempt >= 3) {
                    deadLetters.add(event);
                    System.out.println("dead-lettered: " + event.id());
                    return;
                }
                throw new IllegalArgumentException("permanent invalid payload");
            }
            appliedEffects++;
            processed.add(event.id());
            System.out.println("applied effect: " + event.id());
        }

        int appliedEffects() {
            return appliedEffects;
        }

        int deadLetterCount() {
            return deadLetters.size();
        }
    }

    public static void main(String[] args) {
        Consumer consumer = new Consumer();
        Event completed = new Event("e-1", "OrderCompleted", "normal");
        consumer.handle(completed, 1);
        consumer.handle(completed, 1);
        check(consumer.appliedEffects() == 1, "duplicate must not repeat effect");

        Event transientEvent = new Event("e-2", "OrderCompleted", "transient");
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                consumer.handle(transientEvent, attempt);
                break;
            } catch (IllegalStateException failure) {
                System.out.println("retry " + attempt + ": " + failure.getMessage());
            }
        }
        check(consumer.appliedEffects() == 2, "transient event eventually applies");

        Event poison = new Event("e-3", "OrderCompleted", "poison");
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                consumer.handle(poison, attempt);
                break;
            } catch (IllegalArgumentException failure) {
                System.out.println("permanent failure " + attempt);
            }
        }
        check(consumer.deadLetterCount() == 1, "poison event must be visible in DLQ");
        System.out.println("PASS: effects=" + consumer.appliedEffects()
                + ", deadLetters=" + consumer.deadLetterCount());
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
