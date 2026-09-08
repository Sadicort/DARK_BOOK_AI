package darkbook.scanner;

import darkbook.events.DarkBookEventBus;
import darkbook.knowledge.KnowledgeBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** Controls scanner lifecycle and delegates every collected record to the knowledge pipeline. */
public final class ScannerManager implements AutoCloseable {
    public enum State { STOPPED, RUNNING, PAUSED, ERROR }

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "darkbook-scanner"); thread.setDaemon(true); return thread;
    });
    private final KnowledgeBuilder knowledge;
    private final DarkBookEventBus events;
    private final AtomicLong seen = new AtomicLong();
    private final AtomicLong saved = new AtomicLong();
    private final AtomicLong errors = new AtomicLong();
    private volatile State state = State.STOPPED;
    private volatile Instant startedAt;
    private volatile VideoSource source;
    private ScheduledFuture<?> task;

    public ScannerManager(KnowledgeBuilder knowledge, DarkBookEventBus events) {
        this.knowledge = knowledge; this.events = events;
    }

    public synchronized void start(VideoSource newSource, long intervalSeconds, long maxVideos) {
        stop(); source = newSource; state = State.RUNNING; startedAt = Instant.now();
        task = executor.scheduleWithFixedDelay(() -> scanOnce(maxVideos), 0, Math.max(1, intervalSeconds), TimeUnit.SECONDS);
        events.publish("ScannerStarted", "scanner", Map.of("source", newSource.name()));
    }

    public synchronized void pause() {
        if (state == State.RUNNING) { state = State.PAUSED; events.publish("ScannerPaused", "scanner", Map.of()); }
    }

    public synchronized void resume() {
        if (state == State.PAUSED) { state = State.RUNNING; events.publish("ScannerResumed", "scanner", Map.of()); }
    }

    public synchronized void stop() {
        if (task != null) task.cancel(false);
        task = null;
        if (source != null) try { source.close(); } catch (Exception ignored) { }
        source = null;
        if (state != State.STOPPED) events.publish("ScannerStopped", "scanner", Map.of("saved", saved.get()));
        state = State.STOPPED;
    }

    public Status status() {
        long seconds = startedAt == null ? 0 : Duration.between(startedAt, Instant.now()).toSeconds();
        return new Status(state, seen.get(), saved.get(), errors.get(), seconds, source == null ? "—" : source.name());
    }

    private void scanOnce(long maxVideos) {
        if (state != State.RUNNING) return;
        if (maxVideos > 0 && saved.get() >= maxVideos) { stop(); return; }
        try {
            Optional<darkbook.models.VideoRecord> next = source.collectNext();
            if (next.isEmpty()) { stop(); return; }
            seen.incrementAndGet(); knowledge.ingest(next.get()); saved.incrementAndGet();
        } catch (Exception exception) {
            errors.incrementAndGet(); state = State.ERROR;
            events.publish("ErrorDetected", "scanner", Map.of("message", exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage()));
        }
    }

    @Override public void close() { stop(); executor.shutdownNow(); }
    public record Status(State state, long videosSeen, long videosSaved, long errors, long activeSeconds, String source) { }
}

