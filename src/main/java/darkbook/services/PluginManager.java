package darkbook.services;

import com.fasterxml.jackson.databind.JsonNode;
import darkbook.utils.AppPaths;
import darkbook.utils.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** Discovers declarative plugin manifests without executing untrusted plugin code. */
public final class PluginManager {
    private final AppPaths paths;
    private volatile List<PluginDescriptor> descriptors = List.of();

    public PluginManager(AppPaths paths) { this.paths = paths; }

    public synchronized void scan() throws IOException {
        List<PluginDescriptor> found = new ArrayList<>();
        try (var stream = Files.walk(paths.resolve("plugins"), 2)) {
            for (var manifest : stream.filter(path -> path.getFileName().toString().equals("plugin.json")).toList()) {
                try {
                    JsonNode node = Json.MAPPER.readTree(Files.readString(manifest));
                    String id = node.path("id").asText("").trim(); String name = node.path("name").asText("").trim();
                    if (!id.matches("[a-z][a-z0-9-]{1,63}") || name.isEmpty()) continue;
                    found.add(new PluginDescriptor(id, name, node.path("version").asText("0.0.0"),
                            node.path("dashboardPage").asText(""), false));
                } catch (RuntimeException ignored) { }
            }
        }
        descriptors = List.copyOf(found);
    }

    public List<PluginDescriptor> descriptors() { return descriptors; }
    public record PluginDescriptor(String id, String name, String version, String dashboardPage, boolean codeLoaded) { }
}

