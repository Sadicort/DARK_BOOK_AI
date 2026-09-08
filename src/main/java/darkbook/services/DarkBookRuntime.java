package darkbook.services;

import com.fasterxml.jackson.databind.JsonNode;
import darkbook.automation.PlaywrightVideoSource;
import darkbook.config.ConfigurationManager;
import darkbook.database.DatabaseManager;
import darkbook.database.VideoRepository;
import darkbook.datasets.DatasetBuilder;
import darkbook.events.DarkBookEventBus;
import darkbook.intelligence.ContentAnalyzer;
import darkbook.knowledge.KnowledgeBuilder;
import darkbook.knowledge.KnowledgeGraphEngine;
import darkbook.knowledge.MarkdownWriter;
import darkbook.learning.AutoLearningEngine;
import darkbook.memory.MemoryRepository;
import darkbook.neural.EmbeddingEngine;
import darkbook.neural.PythonTrainerManager;
import darkbook.neural.InferenceEngine;
import darkbook.neural.Tokenizer;
import darkbook.prediction.TrendPredictionEngine;
import darkbook.obsidian.ObsidianService;
import darkbook.reasoning.ReasoningEngine;
import darkbook.scanner.DemoVideoSource;
import darkbook.scanner.ScannerManager;
import darkbook.scanner.VideoSource;
import darkbook.utils.AppPaths;
import darkbook.vision.VisionEngine;
import darkbook.voice.VoiceEngine;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Composition root: initializes services in boot order and exposes controlled application-level operations. */
public final class DarkBookRuntime implements AutoCloseable {
    private final AppPaths paths = AppPaths.fromWorkingDirectory();
    private final DarkBookEventBus events = new DarkBookEventBus();
    private ConfigurationManager configuration;
    private DatabaseManager databases;
    private VideoRepository videos;
    private MemoryRepository memories;
    private KnowledgeGraphEngine graph;
    private KnowledgeBuilder knowledge;
    private ReasoningEngine reasoning;
    private ScannerManager scanner;
    private PythonTrainerManager trainer;
    private InferenceEngine inference;
    private AutoLearningEngine learning;
    private VisionEngine vision;
    private VoiceEngine voice;
    private SystemMonitor systemMonitor;
    private LogService logs;
    private PluginManager plugins;
    private ContentAnalyzer analyzer;

    public void initialize() throws Exception {
        paths.initialize(); events.publish("ModuleLoading", "bootstrap", Map.of("module", "Configuration Manager"));
        logs = new LogService(paths, events); configuration = new ConfigurationManager(paths, events); configuration.initialize();
        events.publish("ModuleLoading", "bootstrap", Map.of("module", "SQLite"));
        databases = new DatabaseManager(paths); databases.initialize();
        Tokenizer tokenizer = new Tokenizer(); analyzer = new ContentAnalyzer(tokenizer);
        int dimension = configuration.ai().path("embeddingDimension").asInt(64);
        EmbeddingEngine embeddings = new EmbeddingEngine(dimension, tokenizer, events);
        inference = new InferenceEngine(paths, embeddings);
        videos = new VideoRepository(databases); memories = new MemoryRepository(databases, embeddings);
        graph = new KnowledgeGraphEngine(databases);
        knowledge = new KnowledgeBuilder(videos, new MarkdownWriter(paths), new ObsidianService(paths), new DatasetBuilder(paths, events), embeddings,
                memories, graph, new TrendPredictionEngine(databases, events), events);
        reasoning = new ReasoningEngine(memories); scanner = new ScannerManager(knowledge, events);
        trainer = new PythonTrainerManager(paths, configuration, events); learning = new AutoLearningEngine(events);
        vision = new VisionEngine(paths, events); voice = new VoiceEngine(paths, events); systemMonitor = new SystemMonitor(paths.root());
        plugins = new PluginManager(paths); plugins.scan();
        events.publish("SystemReady", "bootstrap", Map.of("version", "1.0.0"));
    }

    public void startScanner() throws Exception {
        JsonNode config = configuration.scanner();
        String mode = config.path("mode").asText("demo");
        VideoSource source;
        if ("playwright".equalsIgnoreCase(mode)) {
            List<String> urls = new ArrayList<>(); config.path("seedUrls").forEach(node -> urls.add(node.asText()));
            boolean headless = configuration.read("playwright.json").path("headless").asBoolean(true);
            source = new PlaywrightVideoSource(urls, analyzer, headless);
        } else source = new DemoVideoSource(analyzer);
        scanner.start(source, config.path("intervalSeconds").asLong(8), config.path("maxVideos").asLong(100));
    }

    public Map<String, Object> dashboard() throws SQLException, IOException {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("version", "1.0.0"); result.put("videosTotal", videos.count());
        result.put("videosToday", videos.countSince(Instant.now().truncatedTo(ChronoUnit.DAYS)));
        result.put("videosLastHour", videos.countSince(Instant.now().minus(1, ChronoUnit.HOURS)));
        result.put("memories", memories.count()); result.put("learningCycles", learning.cycles());
        result.put("scanner", scanner.status()); result.put("training", trainer.status());
        result.put("recentVideos", videos.latest(8)); result.put("events", events.recent(20));
        result.put("system", systemMonitor.snapshot()); result.put("modules", modules());
        return result;
    }

    public Map<String, Object> modules() {
        return Map.ofEntries(
                Map.entry("configuration", "ONLINE"), Map.entry("sqlite", "ONLINE"), Map.entry("obsidian", "ONLINE"),
                Map.entry("scanner", scanner == null ? "LOADING" : scanner.status().state().name()),
                Map.entry("neural", "ONLINE V1.0"), Map.entry("memory", "ONLINE V2.0"),
                Map.entry("embedding", "ONLINE V1.0"), Map.entry("knowledge", "ONLINE V1.0"),
                Map.entry("reasoning", "ONLINE V1.0"), Map.entry("vision", "READY FOR PROVIDER V1.0"),
                Map.entry("voice", "READY FOR PROVIDER V1.0"), Map.entry("prediction", "ONLINE V1.0"),
                Map.entry("learning", "ONLINE V1.0"), Map.entry("python", trainer.status().state().name()));
    }

    public List<Map<String, Object>> datasets() throws IOException {
        return listFiles("datasets", paths.resolve("datasets"));
    }

    public List<Map<String, Object>> models() throws IOException { return listFiles("models", paths.resolve("models")); }

    public void ingest(darkbook.models.VideoRecord video) throws Exception { knowledge.ingest(video); }
    public List<PluginManager.PluginDescriptor> plugins() { return plugins.descriptors(); }

    private List<Map<String, Object>> listFiles(String rootName, java.nio.file.Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            return stream.filter(Files::isRegularFile).map(file -> {
                try { return Map.<String, Object>of("path", rootName + "/" + root.relativize(file).toString().replace('\\', '/'),
                        "bytes", Files.size(file), "updatedAt", Files.getLastModifiedTime(file).toInstant()); }
                catch (IOException exception) { return Map.<String, Object>of("path", file.toString(), "error", exception.getMessage()); }
            }).toList();
        }
    }

    public AppPaths paths() { return paths; }
    public DarkBookEventBus events() { return events; }
    public ConfigurationManager configuration() { return configuration; }
    public VideoRepository videos() { return videos; }
    public MemoryRepository memories() { return memories; }
    public KnowledgeGraphEngine graph() { return graph; }
    public ReasoningEngine reasoning() { return reasoning; }
    public ScannerManager scanner() { return scanner; }
    public PythonTrainerManager trainer() { return trainer; }
    public InferenceEngine inference() { return inference; }
    public VisionEngine vision() { return vision; }
    public VoiceEngine voice() { return voice; }

    @Override public void close() throws Exception {
        if (scanner != null) scanner.close(); if (trainer != null) trainer.close(); if (learning != null) learning.close();
        if (logs != null) logs.close();
    }
}
