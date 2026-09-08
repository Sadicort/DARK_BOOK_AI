package darkbook.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Defines and creates every persistent path used by Dark Book AI. */
public final class AppPaths {
    private final Path root;

    public AppPaths(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    public static AppPaths fromWorkingDirectory() {
        String configured = System.getProperty("darkbook.home", System.getenv("DARKBOOK_HOME"));
        return new AppPaths(configured == null || configured.isBlank() ? discoverProjectRoot() : Path.of(configured));
    }

    /**
     * Finds the project root when the packaged executable is launched from its app-image
     * directory. This prevents runtime databases and configuration from being created inside
     * {@code build/package/DarkBookAI} after a folder migration.
     */
    private static Path discoverProjectRoot() {
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        for (Path candidate = workingDirectory; candidate != null; candidate = candidate.getParent()) {
            if (Files.isRegularFile(candidate.resolve("web/index.html"))
                    && Files.isRegularFile(candidate.resolve("settings.gradle"))) {
                return candidate;
            }
        }
        return workingDirectory;
    }

    public void initialize() throws IOException {
        for (String directory : List.of(
                "assets", "config", "database", "datasets/raw", "datasets/processed",
                "datasets/vocabulary", "datasets/embeddings", "datasets/exports",
                "knowledge/videos", "knowledge/hashtags", "knowledge/topics", "knowledge/audios",
                "knowledge/people", "knowledge/reports", "knowledge/concepts", "logs",
                "models/neural", "models/checkpoints", "models/embeddings", "models/tokenizer",
                "models/classifier", "obsidian", "plugins", "web/pages", "web/components",
                "web/layouts", "web/styles", "web/scripts", "web/animations", "web/icons",
                "web/fonts", "embeddings", "memory", "exports", "cache", "training", "analytics")) {
            Files.createDirectories(resolve(directory));
        }
    }

    public Path root() { return root; }
    public Path resolve(String relative) { return root.resolve(relative).normalize(); }
    public Path config(String file) { return resolve("config/" + file); }
    public Path database(String file) { return resolve("database/" + file); }
    public Path web(String relative) { return resolve("web/" + relative); }
}
