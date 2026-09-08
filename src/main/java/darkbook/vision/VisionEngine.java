package darkbook.vision;

import darkbook.events.DarkBookEventBus;
import darkbook.utils.AppPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Validates local image inputs and exposes a provider boundary for future OCR/object models. */
public final class VisionEngine {
    public static final String VERSION = "1.0";
    private final AppPaths paths;
    private final DarkBookEventBus events;

    public VisionEngine(AppPaths paths, DarkBookEventBus events) { this.paths = paths; this.events = events; }

    public Analysis inspect(String relativePath) throws IOException {
        Path target = paths.root().resolve(relativePath).normalize();
        if (!target.startsWith(paths.root()) || !Files.isRegularFile(target)) throw new IllegalArgumentException("Invalid image path");
        String mime = Files.probeContentType(target);
        if (mime == null || !mime.startsWith("image/")) throw new IllegalArgumentException("The file is not an image");
        Analysis result = new Analysis("READY_FOR_PROVIDER", mime, Files.size(target),
                "Archivo validado. Configure un proveedor OCR/visión en Python para inferencia avanzada.");
        events.publish("VisionAnalyzed", "vision", Map.of("path", relativePath, "mime", mime)); return result;
    }

    public record Analysis(String state, String mimeType, long bytes, String message) { }
}

