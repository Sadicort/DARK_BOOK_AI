package darkbook.voice;

import darkbook.events.DarkBookEventBus;
import darkbook.utils.AppPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Validates local audio and defines the controlled Python-provider boundary for transcription and emotion. */
public final class VoiceEngine {
    public static final String VERSION = "1.0";
    private final AppPaths paths;
    private final DarkBookEventBus events;

    public VoiceEngine(AppPaths paths, DarkBookEventBus events) { this.paths = paths; this.events = events; }

    public Analysis inspect(String relativePath) throws IOException {
        Path target = paths.root().resolve(relativePath).normalize();
        if (!target.startsWith(paths.root()) || !Files.isRegularFile(target)) throw new IllegalArgumentException("Invalid audio path");
        String mime = Files.probeContentType(target);
        if (mime == null || !mime.startsWith("audio/")) throw new IllegalArgumentException("The file is not audio");
        Analysis result = new Analysis("READY_FOR_PROVIDER", mime, Files.size(target), "Audio validado; transcripción pendiente de proveedor Python configurado.");
        events.publish("VoiceAnalyzed", "voice", Map.of("path", relativePath, "mime", mime)); return result;
    }

    public record Analysis(String state, String mimeType, long bytes, String message) { }
}

