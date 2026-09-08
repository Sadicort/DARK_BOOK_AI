package darkbook.services;

import darkbook.events.DarkBookEventBus;
import darkbook.utils.AppPaths;
import darkbook.utils.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;

/** Appends each global event to a module-specific daily UTF-8 log without overwriting earlier history. */
public final class LogService implements AutoCloseable {
    private final AppPaths paths;
    private final AutoCloseable subscription;

    public LogService(AppPaths paths, DarkBookEventBus events) {
        this.paths = paths;
        this.subscription = events.subscribe(event -> {
            try { append(event.source(), Json.stringify(event)); }
            catch (IOException ignored) { /* Logging must never break the event producer. */ }
        });
    }

    private synchronized void append(String source, String line) throws IOException {
        String safe = source.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase(java.util.Locale.ROOT);
        Files.writeString(paths.resolve("logs/" + safe + "-" + LocalDate.now() + ".log"), line + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    @Override public void close() throws Exception { subscription.close(); }
}

