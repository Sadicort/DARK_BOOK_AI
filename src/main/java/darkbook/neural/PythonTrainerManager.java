package darkbook.neural;

import com.fasterxml.jackson.databind.JsonNode;
import darkbook.config.ConfigurationManager;
import darkbook.events.DarkBookEventBus;
import darkbook.utils.AppPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Lets Java own Python training lifecycle, captures output, and keeps generated artifacts under models/. */
public final class PythonTrainerManager implements AutoCloseable {
    public enum State { IDLE, RUNNING, COMPLETED, FAILED, CANCELLED, UNAVAILABLE }
    private final AppPaths paths;
    private final ConfigurationManager configuration;
    private final DarkBookEventBus events;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "darkbook-python-trainer"); thread.setDaemon(true); return thread;
    });
    private volatile State state = State.IDLE;
    private volatile String message = "Listo";
    private volatile Instant startedAt;
    private volatile Process process;

    public PythonTrainerManager(AppPaths paths, ConfigurationManager configuration, DarkBookEventBus events) {
        this.paths = paths; this.configuration = configuration; this.events = events;
        detectAvailability();
    }

    public synchronized void start() {
        if (state == State.UNAVAILABLE) throw new IllegalStateException(message);
        if (state == State.RUNNING) throw new IllegalStateException("Training is already running");
        state = State.RUNNING; startedAt = Instant.now(); message = "Iniciando Python";
        events.publish("TrainingStarted", "neural", Map.of()); executor.submit(this::run);
    }

    public synchronized void cancel() {
        if (state == State.UNAVAILABLE) return;
        if (process != null && process.isAlive()) process.destroy();
        state = State.CANCELLED; message = "Cancelado por el usuario";
        events.publish("TrainingCancelled", "neural", Map.of());
    }

    public Status status() { return new Status(state, message, startedAt); }

    private void run() {
        try {
            JsonNode config = configuration.read("python.json");
            ProcessBuilder builder = new ProcessBuilder(config.path("executable").asText("python"),
                    paths.resolve(config.path("trainerScript").asText("training/trainer.py")).toString(),
                    "--dataset", paths.resolve("datasets/raw/videos.jsonl").toString(),
                    "--output", paths.resolve("models/neural/darkbook-centroid-v1.json").toString());
            builder.directory(paths.root().toFile()); builder.redirectErrorStream(true); process = builder.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            int exit = process.waitFor();
            if (exit == 0) {
                state = State.COMPLETED; message = output.isBlank() ? "Modelo generado" : output;
                events.publish("TrainingFinished", "neural", Map.of("model", "darkbook-centroid-v1", "output", message));
            } else {
                state = State.FAILED; message = output.isBlank() ? "Python terminó con código " + exit : output;
                events.publish("ErrorDetected", "neural", Map.of("message", message));
            }
        } catch (Exception exception) {
            state = State.FAILED; message = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
            events.publish("ErrorDetected", "neural", Map.of("message", message));
        } finally { process = null; }
    }

    private void detectAvailability() {
        try {
            String executable = configuration.read("python.json").path("executable").asText("python");
            Process check = new ProcessBuilder(executable, "--version").redirectErrorStream(true).start();
            boolean completed = check.waitFor(3, java.util.concurrent.TimeUnit.SECONDS);
            if (!completed) check.destroyForcibly();
            if (!completed || check.exitValue() != 0) throw new IOException("Python version check failed");
            state = State.IDLE; message = "Python 3 disponible";
        } catch (Exception exception) {
            state = State.UNAVAILABLE;
            message = "Python 3 no está instalado o config/python.json no apunta a un ejecutable válido";
        }
    }

    @Override public void close() { cancel(); executor.shutdownNow(); }
    public record Status(State state, String message, Instant startedAt) { }
}
