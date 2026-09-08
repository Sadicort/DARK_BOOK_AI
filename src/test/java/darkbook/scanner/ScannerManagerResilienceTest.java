package darkbook.scanner;

import darkbook.models.VideoRecord;
import darkbook.services.DarkBookRuntime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ScannerManagerResilienceTest {
    @TempDir Path temporary;

    @Test void recoversFromTransientSourceErrorsAndKeepsIngesting() throws Exception {
        String previous = System.getProperty("darkbook.home");
        System.setProperty("darkbook.home", temporary.toString());
        try (DarkBookRuntime runtime = new DarkBookRuntime()) {
            runtime.initialize();

            AtomicInteger calls = new AtomicInteger();
            VideoSource flaky = new VideoSource() {
                @Override public Optional<VideoRecord> collectNext() {
                    int attempt = calls.incrementAndGet();
                    if (attempt <= 2) throw new RuntimeException("fallo transitorio " + attempt);
                    if (attempt == 3) {
                        return Optional.of(new VideoRecord("resilience-1", "test", "https://example.test/v",
                                "IA tutorial #ai", "tester", 1, 1, 1, "tecnología", "positiva",
                                List.of("#ai"), "audio", Instant.now()));
                    }
                    return Optional.empty();
                }
                @Override public String name() { return "flaky"; }
            };

            ScannerManager scanner = runtime.scanner();
            scanner.start(flaky, 0, 1);

            long deadline = System.currentTimeMillis() + 20_000;
            while (runtime.videos().count() < 1 && System.currentTimeMillis() < deadline) Thread.sleep(100);
            scanner.stop();

            assertEquals(1, runtime.videos().count(), "el video del 3.er intento debe persistirse");
            ScannerManager.Status status = scanner.status();
            assertEquals(2, status.errors(), "los dos fallos transitorios se contabilizan");
            assertEquals(1, status.videosSaved());
            assertNotEquals(ScannerManager.State.ERROR, status.state(), "el scanner no queda latcheado en ERROR");
        } finally {
            if (previous == null) System.clearProperty("darkbook.home"); else System.setProperty("darkbook.home", previous);
        }
    }
}
