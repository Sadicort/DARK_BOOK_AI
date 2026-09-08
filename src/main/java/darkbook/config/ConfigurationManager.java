package darkbook.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import darkbook.events.DarkBookEventBus;
import darkbook.utils.AppPaths;
import darkbook.utils.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Owns persistent JSON configuration. Inputs and outputs are JSON trees; changes emit ConfigurationUpdated. */
public final class ConfigurationManager {
    private static final Map<String, String> DEFAULTS = defaults();
    private final AppPaths paths;
    private final DarkBookEventBus events;

    public ConfigurationManager(AppPaths paths, DarkBookEventBus events) {
        this.paths = paths;
        this.events = events;
    }

    public void initialize() throws IOException {
        for (var entry : DEFAULTS.entrySet()) {
            var target = paths.config(entry.getKey());
            if (Files.notExists(target)) {
                writeAtomically(target, Json.parse(entry.getValue()));
            } else {
                read(entry.getKey());
            }
        }
    }

    public synchronized JsonNode read(String file) throws IOException {
        validate(file);
        return Json.MAPPER.readTree(Files.readString(paths.config(file)));
    }

    public synchronized JsonNode all() throws IOException {
        ObjectNode root = Json.MAPPER.createObjectNode();
        for (String file : DEFAULTS.keySet()) root.set(file.replace(".json", ""), read(file));
        return root;
    }

    public synchronized void update(String file, JsonNode value) throws IOException {
        validate(file);
        if (!value.isObject()) throw new IllegalArgumentException("Configuration must be a JSON object");
        writeAtomically(paths.config(file), value);
        events.publish("ConfigurationUpdated", "config", Map.of("file", file));
    }

    public JsonNode scanner() throws IOException { return read("scanner.json"); }
    public JsonNode ai() throws IOException { return read("ai.json"); }

    private void validate(String file) {
        if (!DEFAULTS.containsKey(file)) throw new IllegalArgumentException("Unknown configuration file: " + file);
    }

    private static void writeAtomically(java.nio.file.Path target, JsonNode value) throws IOException {
        var temporary = target.resolveSibling(target.getFileName() + ".tmp");
        Files.writeString(temporary, Json.MAPPER.writeValueAsString(value));
        try {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Map<String, String> defaults() {
        Map<String, String> files = new LinkedHashMap<>();
        files.put("config.json", """
                {"version":"1.0.0","language":"es","api":{"host":"127.0.0.1","port":17321},"createdAt":"%s"}
                """.formatted(Instant.now()));
        files.put("scanner.json", """
                {"mode":"demo","intervalSeconds":8,"maxVideos":100,"durationHours":10,"infinite":false,"language":"es","region":"DO","seedUrls":[],
                 "watchSecondsMin":8,"watchSecondsMax":15,"skipAds":true,"skipDuplicates":true,"humanBehavior":true,"captchaPauseSeconds":30,"maxConsecutiveSkips":40}
                """);
        files.put("ui.json", """
                {"theme":"dark-book-purple","animations":true,"reducedMotion":false,"compactMode":false}
                """);
        files.put("ai.json", """
                {"embeddingDimension":64,"learningRate":0.01,"batchSize":16,"epochs":10,"activeModel":"darkbook-centroid-v1","autoLearn":true}
                """);
        files.put("obsidian.json", """
                {"vaultPath":"obsidian","sync":true,"autoSave":true}
                """);
        files.put("python.json", """
                {"executable":"python","trainerScript":"training/trainer.py","timeoutMinutes":30}
                """);
        files.put("playwright.json", """
                {"headless":false,"timeoutMs":30000,"proxy":"","respectRateLimits":true,"channel":"chrome","userDataDir":"userdata","autoInstallBrowser":true}
                """);
        return files;
    }
}

