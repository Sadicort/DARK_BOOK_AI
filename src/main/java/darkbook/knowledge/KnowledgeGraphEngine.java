package darkbook.knowledge;

import darkbook.database.DatabaseManager;
import darkbook.models.VideoRecord;
import darkbook.utils.Json;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Maintains normalized nodes and weighted relations between videos, people, topics, hashtags and audio. */
public final class KnowledgeGraphEngine {
    public static final String VERSION = "1.0";
    private final DatabaseManager databases;

    public KnowledgeGraphEngine(DatabaseManager databases) { this.databases = databases; }

    public void connect(VideoRecord video) throws SQLException {
        try (Connection connection = databases.connect("memory.db")) {
            connection.setAutoCommit(false);
            upsertNode(connection, "video:" + video.id(), "video", video.description(), Map.of("url", video.url()));
            linkEntity(connection, video.id(), "person", video.author(), "created_by");
            linkEntity(connection, video.id(), "topic", video.category(), "classified_as");
            if (video.audio() != null && !video.audio().isBlank()) linkEntity(connection, video.id(), "audio", video.audio(), "uses_audio");
            for (String hashtag : video.hashtags()) linkEntity(connection, video.id(), "hashtag", hashtag, "tagged_with");
            connection.commit();
        }
    }

    public Map<String, Object> snapshot(int limit) throws SQLException {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        try (Connection connection = databases.connect("memory.db"); var nodeStatement = connection.prepareStatement(
                "SELECT id,type,label,frequency,updated_at FROM graph_nodes ORDER BY frequency DESC LIMIT ?")) {
            nodeStatement.setInt(1, Math.max(1, Math.min(limit, 1000)));
            try (ResultSet rows = nodeStatement.executeQuery()) {
                while (rows.next()) nodes.add(Map.of("id", rows.getString(1), "type", rows.getString(2),
                        "label", rows.getString(3), "frequency", rows.getInt(4), "updatedAt", rows.getString(5)));
            }
            try (var edgeStatement = connection.prepareStatement("SELECT source_id,target_id,relation,weight FROM graph_edges LIMIT ?")) {
                edgeStatement.setInt(1, Math.max(1, Math.min(limit * 3, 3000)));
                try (ResultSet rows = edgeStatement.executeQuery()) {
                    while (rows.next()) edges.add(Map.of("source", rows.getString(1), "target", rows.getString(2),
                            "relation", rows.getString(3), "weight", rows.getDouble(4)));
                }
            }
        }
        return Map.of("nodes", nodes, "edges", edges);
    }

    private void linkEntity(Connection connection, String videoId, String type, String label, String relation) throws SQLException {
        String entityId = type + ":" + label.toLowerCase(java.util.Locale.ROOT).replaceAll("\\s+", "-");
        upsertNode(connection, entityId, type, label, Map.of());
        try (var statement = connection.prepareStatement("""
                INSERT INTO graph_edges(source_id,target_id,relation,weight,updated_at) VALUES(?,?,?,?,?)
                ON CONFLICT(source_id,target_id,relation) DO UPDATE SET weight=weight+1, updated_at=excluded.updated_at
                """)) {
            statement.setString(1, "video:" + videoId); statement.setString(2, entityId);
            statement.setString(3, relation); statement.setDouble(4, 1); statement.setString(5, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    private void upsertNode(Connection connection, String id, String type, String label, Map<String, ?> metadata) throws SQLException {
        try (var statement = connection.prepareStatement("""
                INSERT INTO graph_nodes(id,type,label,metadata_json,frequency,updated_at) VALUES(?,?,?,?,1,?)
                ON CONFLICT(id) DO UPDATE SET frequency=frequency+1, metadata_json=excluded.metadata_json, updated_at=excluded.updated_at
                """)) {
            statement.setString(1, id); statement.setString(2, type); statement.setString(3, label);
            statement.setString(4, Json.stringify(metadata)); statement.setString(5, Instant.now().toString()); statement.executeUpdate();
        }
    }
}

