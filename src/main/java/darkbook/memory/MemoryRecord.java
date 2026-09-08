package darkbook.memory;

import java.time.Instant;

/** Searchable semantic or episodic memory returned with a cosine similarity score. */
public record MemoryRecord(long id, String kind, String sourceId, String content, double importance,
                           Instant createdAt, double similarity) { }

