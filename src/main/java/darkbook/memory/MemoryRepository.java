package darkbook.memory;

import darkbook.database.DatabaseManager;
import darkbook.neural.EmbeddingEngine;
import darkbook.utils.Json;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Stores durable vector memories in memory.db and performs deterministic local cosine retrieval. */
public final class MemoryRepository {
    private final DatabaseManager databases;
    private final EmbeddingEngine embeddings;

    public MemoryRepository(DatabaseManager databases, EmbeddingEngine embeddings) {
        this.databases = databases;
        this.embeddings = embeddings;
    }

    public long save(String kind, String sourceId, String content, double importance) throws SQLException {
        String now = Instant.now().toString();
        try (Connection connection = databases.connect("memory.db"); var statement = connection.prepareStatement(
                "INSERT INTO memories(kind,source_id,content,embedding_json,importance,created_at,last_accessed_at) VALUES(?,?,?,?,?,?,?)",
                java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, kind); statement.setString(2, sourceId); statement.setString(3, content);
            statement.setString(4, Json.stringify(embeddings.embed(content))); statement.setDouble(5, importance);
            statement.setString(6, now); statement.setString(7, now); statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { return keys.next() ? keys.getLong(1) : -1; }
        }
    }

    public List<MemoryRecord> search(String query, int limit) throws SQLException {
        double[] queryVector = embeddings.embed(query);
        List<MemoryRecord> result = new ArrayList<>();
        try (Connection connection = databases.connect("memory.db"); var statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT * FROM memories")) {
            while (rows.next()) {
                try {
                    double[] vector = Json.MAPPER.readValue(rows.getString("embedding_json"), double[].class);
                    result.add(new MemoryRecord(rows.getLong("id"), rows.getString("kind"), rows.getString("source_id"),
                            rows.getString("content"), rows.getDouble("importance"),
                            Instant.parse(rows.getString("created_at")), EmbeddingEngine.cosine(queryVector, vector)));
                } catch (java.io.IOException exception) {
                    throw new SQLException("Corrupt memory embedding", exception);
                }
            }
        }
        return result.stream().sorted(Comparator.comparingDouble(MemoryRecord::similarity).reversed())
                .limit(Math.max(1, Math.min(limit, 50))).toList();
    }

    public long count() throws SQLException {
        try (Connection connection = databases.connect("memory.db"); var statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM memories")) { return result.getLong(1); }
    }
}

