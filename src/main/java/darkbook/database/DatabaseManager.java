package darkbook.database;

import darkbook.utils.AppPaths;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/** Creates isolated SQLite databases and applies idempotent schemas for each storage responsibility. */
public final class DatabaseManager {
    private final AppPaths paths;

    public DatabaseManager(AppPaths paths) { this.paths = paths; }

    public void initialize() throws SQLException {
        migrate("videos.db", """
                CREATE TABLE IF NOT EXISTS videos (
                  id TEXT PRIMARY KEY, platform TEXT NOT NULL, url TEXT NOT NULL,
                  description TEXT NOT NULL, author TEXT NOT NULL, likes INTEGER NOT NULL,
                  comments INTEGER NOT NULL, shares INTEGER NOT NULL, category TEXT NOT NULL,
                  emotion TEXT NOT NULL, hashtags_json TEXT NOT NULL, audio TEXT NOT NULL,
                  collected_at TEXT NOT NULL
                );
                CREATE INDEX IF NOT EXISTS idx_videos_collected ON videos(collected_at DESC);
                CREATE INDEX IF NOT EXISTS idx_videos_category ON videos(category);
                """);
        migrate("memory.db", """
                CREATE TABLE IF NOT EXISTS memories (
                  id INTEGER PRIMARY KEY AUTOINCREMENT, kind TEXT NOT NULL, source_id TEXT,
                  content TEXT NOT NULL, embedding_json TEXT NOT NULL, importance REAL NOT NULL,
                  created_at TEXT NOT NULL, last_accessed_at TEXT NOT NULL, access_count INTEGER NOT NULL DEFAULT 0
                );
                CREATE INDEX IF NOT EXISTS idx_memories_kind ON memories(kind);
                CREATE TABLE IF NOT EXISTS graph_nodes (
                  id TEXT PRIMARY KEY, type TEXT NOT NULL, label TEXT NOT NULL, metadata_json TEXT NOT NULL,
                  frequency INTEGER NOT NULL DEFAULT 1, updated_at TEXT NOT NULL
                );
                CREATE TABLE IF NOT EXISTS graph_edges (
                  source_id TEXT NOT NULL, target_id TEXT NOT NULL, relation TEXT NOT NULL,
                  weight REAL NOT NULL DEFAULT 1, updated_at TEXT NOT NULL,
                  PRIMARY KEY(source_id, target_id, relation)
                );
                """);
        migrate("analytics.db", """
                CREATE TABLE IF NOT EXISTS predictions (
                  id INTEGER PRIMARY KEY AUTOINCREMENT, source_id TEXT, score REAL NOT NULL,
                  confidence REAL NOT NULL, explanation TEXT NOT NULL, created_at TEXT NOT NULL
                );
                CREATE TABLE IF NOT EXISTS daily_metrics (
                  day TEXT NOT NULL, metric TEXT NOT NULL, value REAL NOT NULL,
                  PRIMARY KEY(day, metric)
                );
                """);
        migrate("settings.db", """
                CREATE TABLE IF NOT EXISTS settings (
                  key TEXT PRIMARY KEY, value_json TEXT NOT NULL, updated_at TEXT NOT NULL
                );
                CREATE TABLE IF NOT EXISTS model_registry (
                  id TEXT PRIMARY KEY, version TEXT NOT NULL, path TEXT NOT NULL, accuracy REAL,
                  loss REAL, active INTEGER NOT NULL DEFAULT 0, created_at TEXT NOT NULL
                );
                """);
    }

    public Connection connect(String database) throws SQLException {
        if (!Map.of("videos.db", true, "memory.db", true, "analytics.db", true, "settings.db", true).containsKey(database)) {
            throw new IllegalArgumentException("Unknown database: " + database);
        }
        Path file = paths.database(database);
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys=ON");
            statement.execute("PRAGMA busy_timeout=5000");
            statement.execute("PRAGMA journal_mode=WAL");
        }
        return connection;
    }

    private void migrate(String database, String sql) throws SQLException {
        try (Connection connection = connect(database); Statement statement = connection.createStatement()) {
            for (String command : sql.split(";")) {
                if (!command.isBlank()) statement.execute(command);
            }
        }
    }
}
