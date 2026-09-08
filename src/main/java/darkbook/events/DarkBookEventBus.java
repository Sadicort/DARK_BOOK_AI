package darkbook.events;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/** Decouples engines through typed events and retains a bounded recent history for Dark Book OS. */
public final class DarkBookEventBus {
    private static final int MAX_HISTORY = 500;
    private final AtomicLong sequence = new AtomicLong();
    private final ArrayDeque<DarkBookEvent> history = new ArrayDeque<>();
    private final CopyOnWriteArrayList<Consumer<DarkBookEvent>> listeners = new CopyOnWriteArrayList<>();

    public DarkBookEvent publish(String type, String source, Map<String, ?> data) {
        @SuppressWarnings("unchecked") Map<String, Object> payload = (Map<String, Object>) Map.copyOf(data);
        DarkBookEvent event = new DarkBookEvent(sequence.incrementAndGet(), type, source, Instant.now(), payload);
        synchronized (history) {
            history.addFirst(event);
            while (history.size() > MAX_HISTORY) history.removeLast();
        }
        listeners.forEach(listener -> listener.accept(event));
        return event;
    }

    public List<DarkBookEvent> recent(int limit) {
        synchronized (history) {
            return new ArrayList<>(history).subList(0, Math.min(Math.max(limit, 0), history.size()));
        }
    }

    public AutoCloseable subscribe(Consumer<DarkBookEvent> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }
}

