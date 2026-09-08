package darkbook.scanner;

import darkbook.events.DarkBookEventBus;
import darkbook.knowledge.KnowledgeBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Controls scanner lifecycle on a dedicated worker thread and delegates every collected record to the
 * knowledge pipeline. A single failed poll no longer latches the scanner: it retries with exponential
 * backoff and only stops after {@link #MAX_CONSECUTIVE_ERRORS} consecutive failures.
 */
public final class ScannerManager implements AutoCloseable {
    public enum State { STOPPED, RUNNING, PAUSED, ERROR }

    private static final int MAX_CONSECUTIVE_ERRORS = 5;
    private static final long MAX_BACKOFF_MILLIS = 60_000;

    private final KnowledgeBuilder knowledge;
    private final DarkBookEventBus events;
    private final AtomicLong seen = new AtomicLong();
    private final AtomicLong saved = new AtomicLong();
    private final AtomicLong errors = new AtomicLong();

    private volatile State state = State.STOPPED;
    private volatile Instant startedAt;
    private volatile VideoSource source;
    private volatile String lastError = "";
    private volatile long intervalSeconds = 8;
    private volatile long maxVideos = 0;
    private Thread worker;

    public ScannerManager(KnowledgeBuilder knowledge, DarkBookEventBus events) {
        this.knowledge = knowledge; this.events = events;
    }

    public synchronized void start(VideoSource newSource, long intervalSeconds, long maxVideos) {
        stop();
        this.source = newSource;
        this.intervalSeconds = Math.max(0, intervalSeconds);
        this.maxVideos = Math.max(0, maxVideos);
        this.lastError = "";
        this.startedAt = Instant.now();
        this.state = State.RUNNING;
        this.worker = new Thread(this::runLoop, "darkbook-scanner");
        this.worker.setDaemon(true);
        this.worker.start();
        events.publish("ScannerStarted", "scanner", Map.of("source", newSource.name()));
    }

    public synchronized void pause() {
        if (state == State.RUNNING) { state = State.PAUSED; events.publish("ScannerPaused", "scanner", Map.of()); }
    }

    public synchronized void resume() {
        if (state == State.PAUSED || state == State.ERROR) {
            state = State.RUNNING; events.publish("ScannerResumed", "scanner", Map.of());
        }
    }

    public synchronized void stop() {
        Thread current = worker;
        worker = null;
        boolean wasActive = state != State.STOPPED;
        state = State.STOPPED;
        if (current != null) {
            current.interrupt();
            try { current.join(5_000); } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
        }
        VideoSource closing = source;
        source = null;
        if (closing != null) try { closing.close(); } catch (Exception ignored) { /* nothing to recover */ }
        if (wasActive) events.publish("ScannerStopped", "scanner", Map.of("saved", saved.get()));
    }

    public Status status() {
        long seconds = startedAt == null ? 0 : Duration.between(startedAt, Instant.now()).toSeconds();
        VideoSource active = source;
        return new Status(state, seen.get(), saved.get(), errors.get(), seconds,
                active == null ? "—" : active.name(), lastError);
    }

    // ------------------------------------------------------------------ worker loop

    private void runLoop() {
        int consecutiveErrors = 0;
        while (state != State.STOPPED) {
            if (state == State.PAUSED) { if (!sleepMillis(200)) break; continue; }

            if (maxVideos > 0 && saved.get() >= maxVideos) { closeAsync(); return; }

            try {
                VideoSource active = source;
                if (active == null) break;
                Optional<darkbook.models.VideoRecord> next = active.collectNext();
                if (next.isEmpty()) { closeAsync(); return; }

                seen.incrementAndGet();
                knowledge.ingest(next.get());
                saved.incrementAndGet();
                consecutiveErrors = 0;
                if (state == State.ERROR) state = State.RUNNING;
                if (!sleepMillis(intervalSeconds * 1_000)) break;
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception failure) {
                consecutiveErrors++;
                errors.incrementAndGet();
                lastError = failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage();
                events.publish("ErrorDetected", "scanner", Map.of("message", lastError, "consecutive", consecutiveErrors));
                if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
                    state = State.ERROR;
                    events.publish("ScannerStopped", "scanner", Map.of("reason", "too many errors", "saved", saved.get()));
                    return;
                }
                state = State.ERROR;
                long backoff = Math.min(MAX_BACKOFF_MILLIS, 1_000L * (1L << (consecutiveErrors - 1)));
                if (!sleepMillis(backoff)) break;
                if (state == State.ERROR) state = State.RUNNING;
            }
        }
    }

    /** Sleeps unless a stop is requested or the thread is interrupted; returns false if the loop must exit. */
    private boolean sleepMillis(long millis) {
        long deadline = System.currentTimeMillis() + Math.max(0, millis);
        do {
            if (state == State.STOPPED || Thread.currentThread().isInterrupted()) return false;
            try { Thread.sleep(Math.min(200, Math.max(1, deadline - System.currentTimeMillis()))); }
            catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); return false; }
        } while (System.currentTimeMillis() < deadline);
        return state != State.STOPPED;
    }

    private void closeAsync() {
        VideoSource closing = source;
        source = null;
        state = State.STOPPED;
        if (closing != null) try { closing.close(); } catch (Exception ignored) { /* nothing to recover */ }
        events.publish("ScannerStopped", "scanner", Map.of("saved", saved.get()));
    }

    @Override public void close() { stop(); }

    public record Status(State state, long videosSeen, long videosSaved, long errors, long activeSeconds,
                         String source, String lastError) { }
}
