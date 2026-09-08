package darkbook.datasets;

import darkbook.events.DarkBookEventBus;
import darkbook.models.VideoRecord;
import darkbook.utils.AppPaths;
import darkbook.utils.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/** Converts canonical videos to append-only JSONL training examples and emits DatasetUpdated. */
public final class DatasetBuilder {
    private final AppPaths paths;
    private final DarkBookEventBus events;

    public DatasetBuilder(AppPaths paths, DarkBookEventBus events) { this.paths = paths; this.events = events; }

    public synchronized void append(VideoRecord video) throws IOException {
        var file = paths.resolve("datasets/raw/videos.jsonl");
        String line = Json.MAPPER.writeValueAsString(Map.of(
                "id", video.id(), "text", video.description(), "category", video.category(),
                "emotion", video.emotion(), "hashtags", video.hashtags(), "engagement", Map.of(
                        "likes", video.likes(), "comments", video.comments(), "shares", video.shares()))) + System.lineSeparator();
        Files.writeString(file, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        events.publish("DatasetUpdated", "dataset", Map.of("dataset", "raw/videos.jsonl", "sourceId", video.id()));
    }
}

