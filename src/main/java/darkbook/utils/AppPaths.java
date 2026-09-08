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
     * Resolves the data root. During development it is the project checkout (so databases and
     * configuration live next to the sources). When the packaged executable runs from an
     * installed location without the project tree, it falls back to a per-user writable
     * directory instead of the current working directory, which may be read-only
     * (for example {@code C:\Program Files\...}).
     */
    private static Path discoverProjectRoot() {
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        for (Path candidate = workingDirectory; candidate != null; candidate = candidate.getParent()) {
            if (Files.isRegularFile(candidate.resolve("web/index.html"))
                    && Files.isRegularFile(candidate.resolve("settings.gradle"))) {
                return candidate;
            }
        }
        return userDataDirectory().orElse(workingDirectory);
    }

    private static java.util.Optional<Path> userDataDirectory() {
        String os = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT);
        String base;
        if (os.contains("win")) {
            base = System.getenv("LOCALAPPDATA");
        } else if (os.contains("mac")) {
            String home = System.getProperty("user.home");
            base = home == null ? null : home + "/Library/Application Support";
        } else {
            base = System.getenv("XDG_DATA_HOME");
            if (base == null || base.isBlank()) {
                String home = System.getProperty("user.home");
                base = home == null ? null : home + "/.local/share";
            }
        }
        if (base == null || base.isBlank()) return java.util.Optional.empty();
        return java.util.Optional.of(Path.of(base, "DarkBookAI"));
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
