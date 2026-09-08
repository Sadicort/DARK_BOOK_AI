package darkbook.events;

import java.time.Instant;
import java.util.Map;

/** Immutable event exposed to internal modules and the Dashboard API. */
public record DarkBookEvent(long sequence, String type, String source, Instant timestamp, Map<String, Object> data) { }

