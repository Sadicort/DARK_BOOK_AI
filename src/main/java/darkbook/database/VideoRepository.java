package darkbook.database;

import darkbook.models.VideoRecord;
import darkbook.models.VideoData;
import darkbook.utils.Json;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Persists and queries canonical videos in videos.db; it never exposes JDBC to the frontend. */
public final class VideoRepository {
    private final DatabaseManager databases;

    public VideoRepository(DatabaseManager databases) { this.databases = databases; }

    /** Compatibility constructor for the original scanner and its legacy database schema. */
    public VideoRepository() { this.databases = null; }

    public void save(VideoRecord video) throws SQLException {
        if (databases == null) {
            throw new IllegalStateException("The canonical repository requires DatabaseManager");
        }
        String sql = """
                INSERT INTO videos(id,platform,url,description,author,likes,comments,shares,category,emotion,hashtags_json,audio,collected_at)
                VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)
                ON CONFLICT(id) DO UPDATE SET likes=excluded.likes, comments=excluded.comments,
                shares=excluded.shares, category=excluded.category, emotion=excluded.emotion,
                hashtags_json=excluded.hashtags_json, audio=excluded.audio, collected_at=excluded.collected_at
                """;
        try (Connection connection = databases.connect("videos.db"); var statement = connection.prepareStatement(sql)) {
            statement.setString(1, video.id()); statement.setString(2, video.platform());
            statement.setString(3, video.url()); statement.setString(4, video.description());
            statement.setString(5, video.author()); statement.setLong(6, video.likes());
            statement.setLong(7, video.comments()); statement.setLong(8, video.shares());
            statement.setString(9, video.category()); statement.setString(10, video.emotion());
            statement.setString(11, Json.stringify(video.hashtags())); statement.setString(12, video.audio());
            statement.setString(13, video.collectedAt().toString()); statement.executeUpdate();
        }
    }

    /**
     * Persists records emitted by the original Playwright scanner without mixing its schema
     * with the canonical {@code videos.db}. New collectors must emit {@link VideoRecord}.
     */
    public void save(VideoData video) {
        String sql = """
                INSERT OR IGNORE INTO videos(
                    url, username, display_name, description, likes, comments, shares,
                    favorites, audio, screenshot, watch_time, category, collected_at
                ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (var statement = DatabaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, video.getVideoUrl());
            statement.setString(2, video.getUsername());
            statement.setString(3, video.getDisplayName());
            statement.setString(4, video.getDescription());
            statement.setString(5, video.getLikes());
            statement.setString(6, video.getComments());
            statement.setString(7, video.getShares());
            statement.setString(8, video.getFavorites());
            statement.setString(9, video.getAudioName());
            statement.setString(10, video.getScreenshotPath());
            statement.setInt(11, video.getWatchTime());
            statement.setString(12, video.getCategory());
            statement.setString(13, video.getCollectedAt().toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to persist a legacy scanner video", exception);
        }
    }

    public long count() throws SQLException {
        try (Connection connection = databases.connect("videos.db"); var statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM videos")) {
            return result.getLong(1);
        }
    }

    public long countSince(Instant since) throws SQLException {
        try (Connection connection = databases.connect("videos.db"); var statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM videos WHERE collected_at >= ?")) {
            statement.setString(1, since.toString());
            try (ResultSet result = statement.executeQuery()) { return result.getLong(1); }
        }
    }

    public List<VideoRecord> latest(int limit) throws SQLException {
        List<VideoRecord> records = new ArrayList<>();
        try (Connection connection = databases.connect("videos.db"); var statement = connection.prepareStatement(
                "SELECT * FROM videos ORDER BY collected_at DESC LIMIT ?")) {
            statement.setInt(1, Math.max(1, Math.min(limit, 500)));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) records.add(map(result));
            }
        }
        return records;
    }

    private static VideoRecord map(ResultSet result) throws SQLException {
        try {
            List<String> hashtags = Json.MAPPER.readerForListOf(String.class).readValue(result.getString("hashtags_json"));
            return new VideoRecord(result.getString("id"), result.getString("platform"), result.getString("url"),
                    result.getString("description"), result.getString("author"), result.getLong("likes"),
                    result.getLong("comments"), result.getLong("shares"), result.getString("category"),
                    result.getString("emotion"), hashtags, result.getString("audio"),
                    Instant.parse(result.getString("collected_at")));
        } catch (java.io.IOException exception) {
            throw new SQLException("Corrupt hashtag JSON", exception);
        }
    }
}
