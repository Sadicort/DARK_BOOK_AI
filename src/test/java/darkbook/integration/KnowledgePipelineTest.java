package darkbook.integration;

import darkbook.services.DarkBookRuntime;
import darkbook.models.VideoRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgePipelineTest {
    @TempDir Path temporary;

    @Test void persistsOneVideoAcrossAllCoreStores() throws Exception {
        String previous = System.getProperty("darkbook.home");
        System.setProperty("darkbook.home", temporary.toString());
        try (DarkBookRuntime runtime = new DarkBookRuntime()) {
            runtime.initialize();
            var video = new VideoRecord("integration-1", "test", "https://example.test/video", "IA tutorial #ai", "tester",
                    100, 20, 5, "tecnología", "positiva", List.of("#ai"), "test-audio", Instant.now());
            runtime.ingest(video);
            assertEquals(1, runtime.videos().count());
            assertEquals(1, runtime.memories().count());
            assertFalse(runtime.memories().search("IA tutorial", 5).isEmpty());
            assertEquals(1, runtime.graph().snapshot(20).get("edges") instanceof List<?> edges ? edges.size() > 0 ? 1 : 0 : 0);
            assertTrue(Files.exists(temporary.resolve("obsidian/videos/integration-1.md")));
            assertTrue(Files.exists(temporary.resolve("datasets/raw/videos.jsonl")));
        } finally {
            if (previous == null) System.clearProperty("darkbook.home"); else System.setProperty("darkbook.home", previous);
        }
    }
}

