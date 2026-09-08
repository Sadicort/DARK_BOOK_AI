package darkbook.learning;

import darkbook.events.DarkBookEventBus;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/** Tracks completed automatic learning cycles triggered after durable video ingestion. */
public final class AutoLearningEngine implements AutoCloseable {
    public static final String VERSION = "1.0";
    private final AtomicLong cycles = new AtomicLong();
    private final AutoCloseable subscription;

    public AutoLearningEngine(DarkBookEventBus events) {
        subscription = events.subscribe(event -> {
            if ("VideoCollected".equals(event.type())) {
                long cycle = cycles.incrementAndGet();
                events.publish("LearningCycleCompleted", "learning", Map.of("cycle", cycle, "sourceId", event.data().get("id")));
            }
        });
    }

    public long cycles() { return cycles.get(); }
    @Override public void close() throws Exception { subscription.close(); }
}

