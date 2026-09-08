package darkbook.knowledge;

import darkbook.database.VideoRepository;
import darkbook.datasets.DatasetBuilder;
import darkbook.events.DarkBookEventBus;
import darkbook.memory.MemoryRepository;
import darkbook.models.VideoRecord;
import darkbook.neural.EmbeddingEngine;
import darkbook.prediction.TrendPredictionEngine;
import darkbook.obsidian.ObsidianService;

import java.util.Map;

/** Orchestrates the durable Scanner → knowledge → dataset → memory → prediction pipeline. */
public final class KnowledgeBuilder {
    private final VideoRepository videos;
    private final MarkdownWriter markdown;
    private final DatasetBuilder datasets;
    private final ObsidianService obsidian;
    private final EmbeddingEngine embeddings;
    private final MemoryRepository memories;
    private final KnowledgeGraphEngine graph;
    private final TrendPredictionEngine predictions;
    private final DarkBookEventBus events;

    public KnowledgeBuilder(VideoRepository videos, MarkdownWriter markdown, ObsidianService obsidian, DatasetBuilder datasets,
                            EmbeddingEngine embeddings, MemoryRepository memories, KnowledgeGraphEngine graph,
                            TrendPredictionEngine predictions, DarkBookEventBus events) {
        this.videos = videos; this.markdown = markdown; this.obsidian = obsidian; this.datasets = datasets; this.embeddings = embeddings;
        this.memories = memories; this.graph = graph; this.predictions = predictions; this.events = events;
    }

    public synchronized void ingest(VideoRecord video) throws Exception {
        videos.save(video);
        markdown.writeVideo(video);
        obsidian.mirrorVideo(video);
        datasets.append(video);
        embeddings.create(video.id(), video.description());
        memories.save("episodic", video.id(), video.description(), 0.7);
        graph.connect(video);
        predictions.predict(video);
        events.publish("VideoCollected", "knowledge", Map.of("id", video.id(), "author", video.author(), "category", video.category()));
        events.publish("MemoryStored", "memory", Map.of("sourceId", video.id(), "kind", "episodic"));
    }
}
