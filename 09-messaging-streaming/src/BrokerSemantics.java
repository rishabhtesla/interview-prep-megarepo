import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Local, dependency-free simulations of broker and stream vocabulary.
 *
 * These examples show semantics, not wire compatibility with Kafka, RabbitMQ,
 * SQS, Flink, or Akka. Replace each boundary with the real client/runtime in a
 * production service.
 */
public final class BrokerSemantics {
    private BrokerSemantics() {
    }

    private record Event(String key, String body) {
    }

    private static final class KafkaLike {
        private final List<List<Event>> partitions;

        private KafkaLike(int partitionCount) {
            partitions = new ArrayList<>();
            for (int index = 0; index < partitionCount; index++) {
                partitions.add(new ArrayList<>());
            }
        }

        private void publish(Event event) {
            int partition = Math.floorMod(event.key().hashCode(), partitions.size());
            partitions.get(partition).add(event);
        }

        private List<Event> readPartition(int partition) {
            return List.copyOf(partitions.get(partition));
        }
    }

    private static final class RabbitLike {
        private final Map<String, Queue<Event>> queues = new LinkedHashMap<>();

        private RabbitLike(String... queueNames) {
            for (String queueName : queueNames) {
                queues.put(queueName, new ArrayDeque<>());
            }
        }

        private void publish(String routingKey, Event event) {
            if (routingKey.startsWith("order.")) {
                queues.get("orders").add(event);
            }
            if (routingKey.startsWith("incident.")) {
                queues.get("incidents").add(event);
            }
        }

        private Event consume(String queueName) {
            return queues.get(queueName).poll();
        }
    }

    private static final class SqsLike {
        private record Delivery(Event event, int visibleAt, int receives) {
        }

        private final List<Delivery> deliveries = new ArrayList<>();
        private int now;

        private void send(Event event) {
            deliveries.add(new Delivery(event, now, 0));
        }

        private Event receive() {
            for (int index = 0; index < deliveries.size(); index++) {
                Delivery delivery = deliveries.get(index);
                if (delivery.visibleAt() <= now) {
                    deliveries.set(index, new Delivery(
                            delivery.event(), now + 2, delivery.receives() + 1));
                    return delivery.event();
                }
            }
            return null;
        }

        private void delete(Event event) {
            deliveries.removeIf(delivery -> delivery.event().equals(event));
        }

        private void advanceTime(int amount) {
            now += amount;
        }
    }

    private static final class EventTimeWindows {
        private final Map<Integer, Integer> counts = new HashMap<>();
        private int watermark = Integer.MIN_VALUE;

        private void process(int eventTime) {
            if (eventTime <= watermark) {
                System.out.println("late event dropped by policy: t=" + eventTime);
                return;
            }
            int windowStart = eventTime / 10 * 10;
            counts.merge(windowStart, 1, Integer::sum);
        }

        private void advanceWatermark(int newWatermark) {
            watermark = newWatermark;
            counts.entrySet().removeIf(entry -> {
                int windowEnd = entry.getKey() + 10;
                if (windowEnd <= watermark) {
                    System.out.println("closed window [" + entry.getKey() + ", "
                            + windowEnd + "): " + entry.getValue());
                    return true;
                }
                return false;
            });
        }
    }

    private static final class ActorLike {
        private final Map<String, Integer> incidentCounts = new HashMap<>();

        private void tell(String incidentId) {
            incidentCounts.merge(incidentId, 1, Integer::sum);
            System.out.println("actor " + incidentId + " owns state="
                    + incidentCounts.get(incidentId));
        }
    }

    public static void main(String[] args) {
        KafkaLike kafka = new KafkaLike(2);
        kafka.publish(new Event("order-7", "created"));
        kafka.publish(new Event("order-7", "paid"));
        System.out.println("Kafka partition preserves key order: "
                + kafka.readPartition(Math.floorMod("order-7".hashCode(), 2)));

        RabbitLike rabbit = new RabbitLike("orders", "incidents");
        rabbit.publish("order.completed", new Event("order-7", "completed"));
        rabbit.publish("incident.created", new Event("incident-3", "created"));
        System.out.println("Rabbit exchange routed: " + rabbit.consume("orders")
                + " and " + rabbit.consume("incidents"));

        SqsLike sqs = new SqsLike();
        Event message = new Event("job-1", "resize");
        sqs.send(message);
        System.out.println("SQS first receive: " + sqs.receive());
        System.out.println("SQS hidden during visibility timeout: " + sqs.receive());
        sqs.advanceTime(2);
        System.out.println("SQS redelivered after timeout: " + sqs.receive());
        sqs.delete(message);

        EventTimeWindows windows = new EventTimeWindows();
        windows.process(12);
        windows.process(15);
        windows.advanceWatermark(20);
        windows.process(15);

        ActorLike actor = new ActorLike();
        actor.tell("incident-3");
        actor.tell("incident-3");
    }
}
