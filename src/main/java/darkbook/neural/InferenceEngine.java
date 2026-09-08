package darkbook.neural;

import com.fasterxml.jackson.databind.JsonNode;
import darkbook.utils.AppPaths;
import darkbook.utils.Json;

import java.io.IOException;
import java.nio.file.Files;

/** Loads the Python centroid artifact and performs local Java inference without letting Python control the app. */
public final class InferenceEngine {
    private final AppPaths paths;
    private final EmbeddingEngine embeddings;

    public InferenceEngine(AppPaths paths, EmbeddingEngine embeddings) { this.paths = paths; this.embeddings = embeddings; }

    public Prediction predict(String text) throws IOException {
        var modelPath = paths.resolve("models/neural/darkbook-centroid-v1.json");
        if (Files.notExists(modelPath)) return new Prediction(false, "untrained", 0, "Entrena el modelo antes de inferir.");
        JsonNode model = Json.MAPPER.readTree(Files.readString(modelPath));
        double[] input = embeddings.embed(text); String bestLabel = "unknown"; double bestScore = -1;
        var fields = model.path("centroids").fields();
        while (fields.hasNext()) {
            var field = fields.next(); double[] centroid = Json.MAPPER.convertValue(field.getValue(), double[].class);
            double score = EmbeddingEngine.cosine(input, centroid);
            if (score > bestScore) { bestScore = score; bestLabel = field.getKey(); }
        }
        return new Prediction(true, bestLabel, bestScore, "Clasificación por similitud con centroides aprendidos del dataset local.");
    }

    public record Prediction(boolean modelLoaded, String label, double confidence, String explanation) { }
}

