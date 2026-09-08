package darkbook.models;

import java.time.Instant;
import java.util.List;

/** Canonical video data exchanged between scanner, storage, knowledge, dataset and intelligence modules. */
public record VideoRecord(String id, String platform, String url, String description, String author,
                          long likes, long comments, long shares, String category, String emotion,
                          List<String> hashtags, String audio, Instant collectedAt) {
    public VideoRecord {
        hashtags = hashtags == null ? List.of() : List.copyOf(hashtags);
        collectedAt = collectedAt == null ? Instant.now() : collectedAt;
    }
}

