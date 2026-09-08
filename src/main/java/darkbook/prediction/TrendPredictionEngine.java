package darkbook.prediction;

import darkbook.database.DatabaseManager;
import darkbook.events.DarkBookEventBus;
import darkbook.models.VideoRecord;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

/** Produces an explainable baseline viral score and persists every prediction in analytics.db. */
public final class TrendPredictionEngine {
    public static final String VERSION = "1.0";
    private final DatabaseManager databases;
    private final DarkBookEventBus events;

    public TrendPredictionEngine(DatabaseManager databases, DarkBookEventBus events) {
        this.databases = databases; this.events = events;
    }

    public Prediction predict(VideoRecord video) throws SQLException {
        double engagement = Math.log1p(video.likes() + 3.0 * video.comments() + 5.0 * video.shares());
        double score = 1.0 / (1.0 + Math.exp(-(engagement - 8.0) / 2.2));
        double confidence = Math.min(0.92, 0.45 + Math.log1p(video.likes() + video.comments()) / 30.0);
        String explanation = "Puntaje base calculado con likes, comentarios y compartidos; requiere historial temporal para predicción avanzada.";
        try (var connection = databases.connect("analytics.db"); var statement = connection.prepareStatement(
                "INSERT INTO predictions(source_id,score,confidence,explanation,created_at) VALUES(?,?,?,?,?)")) {
            statement.setString(1, video.id()); statement.setDouble(2, score); statement.setDouble(3, confidence);
            statement.setString(4, explanation); statement.setString(5, Instant.now().toString()); statement.executeUpdate();
        }
        events.publish("PredictionCompleted", "prediction", Map.of("sourceId", video.id(), "score", score));
        return new Prediction(video.id(), score, confidence, explanation);
    }

    public record Prediction(String sourceId, double score, double confidence, String explanation) { }
}

