import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shows why an outbox and an inbox solve different failure windows.
 */
public final class OutboxInbox {
    private record OutboxEvent(String id, String payload, boolean published) {
    }

    private final List<String> orders = new ArrayList<>();
    private final List<OutboxEvent> outbox = new ArrayList<>();
    private final Set<String> inbox = new HashSet<>();

    private void createOrder(String orderId) {
        // A real database transaction commits both rows together.
        orders.add(orderId);
        outbox.add(new OutboxEvent("event-" + orderId, "OrderCreated:" + orderId, false));
    }

    private OutboxEvent nextPendingEvent() {
        return outbox.stream()
                .filter(event -> !event.published())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("outbox is empty"));
    }

    private void markPublished(String eventId) {
        for (int index = 0; index < outbox.size(); index++) {
            OutboxEvent event = outbox.get(index);
            if (event.id().equals(eventId)) {
                // A crash between broker publish and this update can duplicate.
                outbox.set(index, new OutboxEvent(event.id(), event.payload(), true));
                return;
            }
        }
        throw new IllegalArgumentException("unknown outbox event: " + eventId);
    }

    private boolean applyInbox(String eventId, String payload) {
        if (!inbox.add(eventId)) {
            return false;
        }
        System.out.println("applied once: " + payload);
        return true;
    }

    public static void main(String[] args) {
        OutboxInbox lesson = new OutboxInbox();
        lesson.createOrder("order-7");
        System.out.println("same local commit created order and outbox: " + lesson.orders);
        OutboxEvent event = lesson.nextPendingEvent();
        System.out.println("publisher sends: " + event.payload());
        lesson.markPublished(event.id());
        lesson.applyInbox(event.id(), event.payload());
        System.out.println("duplicate suppressed: "
                + !lesson.applyInbox(event.id(), event.payload()));
    }
}
